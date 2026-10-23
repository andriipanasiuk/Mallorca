use crate::cards::Card;
use crate::eval::rank;
use crate::measure::play::{commit, committed_at, deal, holes, settle, start, visible};
use crate::measure::policy::{passive, StrategyPolicy};
use crate::measure::stats::{mbb_per_hand, Stats};
use crate::rng::Rng;
use crate::tree::{ActionKind, Node, NodeId, Street, Tree};

pub const DEFAULT_FLOP_RUNOUTS: u32 = 200;
const HAND_COUNT: usize = 1326;
const HALF_POT: f64 = 0.5;
const FULL_POT: f64 = 1.0;

pub trait RangePolicy {
    fn probabilities(&self, node: NodeId, hole: [Card; 2], board: &[Card]) -> Vec<f32>;
}

impl RangePolicy for StrategyPolicy<'_> {
    fn probabilities(&self, node: NodeId, hole: [Card; 2], board: &[Card]) -> Vec<f32> {
        let Node::Decision { street, .. } = self.file.tree.node(node) else {
            panic!("node {node} is not a decision");
        };
        let bucket = self.abstraction.bucket(*street, hole, board);
        self.file
            .probabilities(node, bucket)
            .iter()
            .map(|&p| f32::from(p) / 255.0)
            .collect()
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct LbrConfig {
    pub hands: u64,
    pub seed: u64,
    pub flop_runouts: u32,
    pub stack: u32,
}

impl LbrConfig {
    pub fn new(hands: u64, seed: u64, stack: u32) -> LbrConfig {
        LbrConfig {
            hands,
            seed,
            flop_runouts: DEFAULT_FLOP_RUNOUTS,
            stack,
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq)]
pub struct LbrResult {
    pub mbb_per_hand: f64,
    pub ci95: f64,
    pub by_seat: [f64; 2],
}

const fn all_holes() -> [[Card; 2]; HAND_COUNT] {
    let mut out = [[0; 2]; HAND_COUNT];
    let mut a = 0;
    let mut i = 0;
    while a < 52 {
        let mut b = a + 1;
        while b < 52 {
            out[i] = [a as Card, b as Card];
            i += 1;
            b += 1;
        }
        a += 1;
    }
    out
}

const HOLES: [[Card; 2]; HAND_COUNT] = all_holes();

fn mask(cards: &[Card]) -> u64 {
    cards.iter().fold(0, |m, &c| m | 1 << c)
}

fn touches(hole: [Card; 2], dead: u64) -> bool {
    dead & mask(&hole) != 0
}

#[derive(Debug, Clone)]
struct Range {
    weights: Vec<f64>,
    dead: u64,
}

impl Range {
    fn new(hole: [Card; 2], board: &[Card]) -> Range {
        let mut range = Range {
            weights: vec![0.0; HAND_COUNT],
            dead: mask(&hole) | mask(board),
        };
        range.uniform();
        range
    }

    fn uniform(&mut self) {
        for (w, h) in self.weights.iter_mut().zip(&HOLES) {
            *w = if touches(*h, self.dead) { 0.0 } else { 1.0 };
        }
        self.normalise();
    }

    fn normalise(&mut self) {
        let total: f64 = self.weights.iter().sum();
        if total > 0.0 {
            self.weights.iter_mut().for_each(|w| *w /= total);
        } else {
            self.uniform();
        }
    }

    fn block(&mut self, cards: &[Card]) {
        self.dead |= mask(cards);
        for (w, h) in self.weights.iter_mut().zip(&HOLES) {
            if touches(*h, self.dead) {
                *w = 0.0;
            }
        }
        self.normalise();
    }

    fn observe(&mut self, policy: &dyn RangePolicy, node: NodeId, action: usize, board: &[Card]) {
        for (w, h) in self.weights.iter_mut().zip(&HOLES) {
            if *w > 0.0 {
                *w *= f64::from(policy.probabilities(node, *h, board)[action]);
            }
        }
        self.normalise();
    }
}

fn score(mine: u16, theirs: u16) -> u32 {
    match mine.cmp(&theirs) {
        std::cmp::Ordering::Greater => 2,
        std::cmp::Ordering::Equal => 1,
        std::cmp::Ordering::Less => 0,
    }
}

fn equities(hole: [Card; 2], board: &[Card], flop_runouts: u32, rng: &mut Rng) -> Vec<f64> {
    let dead = mask(&hole) | mask(board);
    let live: Vec<Card> = (0..52).filter(|&c| dead & 1 << c == 0).collect();
    let runouts: Vec<Vec<Card>> = match board.len() {
        5 => vec![Vec::new()],
        4 => live.iter().map(|&r| vec![r]).collect(),
        3 => (0..flop_runouts)
            .map(|_| {
                let n = live.len() as u32;
                let i = rng.below(n) as usize;
                let mut j = rng.below(n - 1) as usize;
                if j >= i {
                    j += 1;
                }
                vec![live[i], live[j]]
            })
            .collect(),
        0 => (0..flop_runouts)
            .map(|_| {
                let mut pool = live.clone();
                for k in 0..5 {
                    let j = k + rng.below((pool.len() - k) as u32) as usize;
                    pool.swap(k, j);
                }
                pool[..5].to_vec()
            })
            .collect(),
        n => panic!("no equity for a board of {n} cards"),
    };
    let mut points = vec![0u32; HAND_COUNT];
    let mut counts = vec![0u32; HAND_COUNT];
    let mut cards = [0 as Card; 7];
    cards[2..2 + board.len()].copy_from_slice(board);
    for runout in &runouts {
        cards[2 + board.len()..].copy_from_slice(runout);
        let blocked = dead | mask(runout);
        cards[..2].copy_from_slice(&hole);
        let mine = rank(&cards);
        for (h, theirs) in HOLES.iter().enumerate() {
            if touches(*theirs, blocked) {
                continue;
            }
            cards[..2].copy_from_slice(theirs);
            points[h] += score(mine, rank(&cards));
            counts[h] += 1;
        }
    }
    points
        .iter()
        .zip(&counts)
        .map(|(&p, &n)| {
            if n == 0 {
                0.0
            } else {
                f64::from(p) / f64::from(2 * n)
            }
        })
        .collect()
}

fn call_value(equity: f64, pot: u32, cost: u32) -> f64 {
    equity * f64::from(pot + cost) - f64::from(cost)
}

fn wager_value(fold_p: f64, equity: f64, pot: u32, wager: u32, pot_called: u32) -> f64 {
    fold_p * f64::from(pot) + (1.0 - fold_p) * (equity * f64::from(pot_called) - f64::from(wager))
}

struct Spot<'a> {
    tree: &'a Tree,
    node: NodeId,
    me: usize,
    committed: [u32; 2],
    stack: u32,
    board: &'a [Card],
}

fn closest_raise(spot: &Spot<'_>, fraction: f64) -> Option<usize> {
    let Node::Decision {
        committed,
        street_bets,
        actions,
        ..
    } = spot.tree.node(spot.node)
    else {
        return None;
    };
    let (me, opp) = (spot.me, 1 - spot.me);
    let pot_after_call =
        f64::from(committed[0] + committed[1] + street_bets[opp] - street_bets[me]);
    let mut best: Option<(f64, usize)> = None;
    for (i, (kind, _)) in actions.iter().enumerate() {
        let ActionKind::RaiseTo(to) = kind else {
            continue;
        };
        let size = (f64::from(*to) - f64::from(street_bets[opp])) / pot_after_call;
        let distance = (size - fraction).abs();
        if best.is_none_or(|(d, _)| distance < d) {
            best = Some((distance, i));
        }
    }
    best.map(|(_, i)| i)
}

fn fold_split(
    opponent: &dyn RangePolicy,
    spot: &Spot<'_>,
    child: NodeId,
    range: &Range,
    equity: &[f64],
) -> (f64, f64) {
    let fold = match spot.tree.node(child) {
        Node::Decision { seat, actions, .. } if usize::from(*seat) != spot.me => {
            actions.iter().position(|(k, _)| *k == ActionKind::Fold)
        }
        _ => None,
    };
    let mut fold_p = 0.0;
    let mut calling = 0.0;
    let mut calling_equity = 0.0;
    for (h, &w) in range.weights.iter().enumerate() {
        if w == 0.0 {
            continue;
        }
        let f = fold.map_or(0.0, |i| {
            f64::from(opponent.probabilities(child, HOLES[h], spot.board)[i])
        });
        fold_p += w * f;
        calling += w * (1.0 - f);
        calling_equity += w * (1.0 - f) * equity[h];
    }
    if calling > 0.0 {
        (fold_p.min(1.0), calling_equity / calling)
    } else {
        (1.0, 0.0)
    }
}

fn choose(opponent: &dyn RangePolicy, spot: &Spot<'_>, range: &Range, equity: &[f64]) -> usize {
    let Node::Decision { actions, .. } = spot.tree.node(spot.node) else {
        panic!("node {} is not a decision", spot.node);
    };
    let find = |want: fn(&ActionKind) -> bool| actions.iter().position(|(k, _)| want(k));
    let candidates = [
        find(|k| *k == ActionKind::Fold),
        find(|k| matches!(k, ActionKind::Check | ActionKind::Call)),
        closest_raise(spot, HALF_POT),
        closest_raise(spot, FULL_POT),
        find(|k| *k == ActionKind::AllIn),
    ];
    let mut tried = Vec::with_capacity(candidates.len());
    let mut best: Option<(f64, usize)> = None;
    let my = spot.committed[spot.me];
    let pot = spot.committed[0] + spot.committed[1];
    let range_equity = range_equity(range, equity);
    for i in candidates.into_iter().flatten() {
        if tried.contains(&i) {
            continue;
        }
        tried.push(i);
        let (kind, child) = actions[i];
        let target = commit(spot.tree, kind, child, spot.committed, spot.me, spot.stack);
        let value = match kind {
            ActionKind::Fold => 0.0,
            ActionKind::Check | ActionKind::Call => call_value(range_equity, pot, target - my),
            ActionKind::RaiseTo(_) | ActionKind::AllIn => {
                let (fold_p, calling_equity) = fold_split(opponent, spot, child, range, equity);
                wager_value(fold_p, calling_equity, pot, target - my, 2 * target)
            }
        };
        if best.is_none_or(|(v, _)| value > v) {
            best = Some((value, i));
        }
    }
    best.map_or_else(|| passive(spot.tree, spot.node), |(_, i)| i)
}

fn range_equity(range: &Range, equity: &[f64]) -> f64 {
    range.weights.iter().zip(equity).map(|(w, e)| w * e).sum()
}

fn preflop_choice(spot: &Spot<'_>, range: &Range, equity: &[f64]) -> usize {
    let Node::Decision { actions, .. } = spot.tree.node(spot.node) else {
        panic!("node {} is not a decision", spot.node);
    };
    let call = passive(spot.tree, spot.node);
    let Some(fold) = actions.iter().position(|(k, _)| *k == ActionKind::Fold) else {
        return call;
    };
    let (kind, child) = actions[call];
    let target = commit(spot.tree, kind, child, spot.committed, spot.me, spot.stack);
    let pot = spot.committed[0] + spot.committed[1];
    let cost = target - spot.committed[spot.me];
    if call_value(range_equity(range, equity), pot, cost) > 0.0 {
        call
    } else {
        fold
    }
}

fn sample(probabilities: &[f32], rng: &mut Rng) -> usize {
    let total: f64 = probabilities.iter().map(|&p| f64::from(p)).sum();
    let mut draw = rng.next_f64() * total;
    for (i, &p) in probabilities.iter().enumerate() {
        let p = f64::from(p);
        if draw < p {
            return i;
        }
        draw -= p;
    }
    probabilities
        .iter()
        .rposition(|&p| p > 0.0)
        .unwrap_or(probabilities.len() - 1)
}

fn play(
    tree: &Tree,
    opponent: &dyn RangePolicy,
    deck: &[Card; 9],
    seat: usize,
    config: &LbrConfig,
    rng: &mut Rng,
) -> i64 {
    let hole = holes(deck);
    let board = &deck[4..9];
    let mut range = Range::new(hole[seat], &[]);
    let mut node = tree.root();
    let mut committed = start(tree, config.stack);
    let mut shown = 0;
    let mut equity: Option<Vec<f64>> = None;
    loop {
        if let Some(chips) = settle(tree, node, committed, &hole, board) {
            return chips[seat];
        }
        let Node::Decision {
            seat: actor,
            street,
            actions,
            ..
        } = tree.node(node)
        else {
            unreachable!("settle returns None only for decisions");
        };
        let n = visible(*street);
        if n > shown {
            range.block(&board[shown..n]);
            shown = n;
            equity = None;
        }
        let seen = &board[..n];
        let actor = usize::from(*actor);
        let choice = if actor != seat {
            let p = opponent.probabilities(node, hole[actor], seen);
            assert_eq!(p.len(), actions.len(), "probabilities at node {node}");
            let a = sample(&p, rng);
            range.observe(opponent, node, a, seen);
            a
        } else {
            let spot = Spot {
                tree,
                node,
                me: seat,
                committed,
                stack: config.stack,
                board: seen,
            };
            let faced = actions.iter().any(|(k, _)| *k == ActionKind::Fold);
            if *street == Street::Preflop && !faced {
                passive(tree, node)
            } else {
                let e = equity
                    .get_or_insert_with(|| equities(hole[seat], seen, config.flop_runouts, rng));
                if *street == Street::Preflop {
                    preflop_choice(&spot, &range, e)
                } else {
                    choose(opponent, &spot, &range, e)
                }
            }
        };
        let (kind, next) = actions[choice];
        committed[actor] = commit(tree, kind, next, committed, actor, config.stack);
        node = next;
    }
}

pub fn lbr_hands(tree: &Tree, opponent: &dyn RangePolicy, config: &LbrConfig) -> Vec<(usize, i64)> {
    let mut rng = Rng::new(config.seed);
    (0..config.hands)
        .map(|i| {
            let seat = (i % 2) as usize;
            let deck = deal(&mut rng);
            (seat, play(tree, opponent, &deck, seat, config, &mut rng))
        })
        .collect()
}

pub fn summarize(tree: &Tree, parts: &[Vec<(usize, i64)>]) -> LbrResult {
    let big_blind = committed_at(tree, tree.root())[1];
    let mut all = Stats::new();
    let mut by_seat = [Stats::new(), Stats::new()];
    for &(seat, chips) in parts.iter().flatten() {
        all.push(chips as f64);
        by_seat[seat].push(chips as f64);
    }
    LbrResult {
        mbb_per_hand: mbb_per_hand(all.mean(), big_blind),
        ci95: mbb_per_hand(all.half_width95(), big_blind),
        by_seat: by_seat.map(|s| mbb_per_hand(s.mean(), big_blind)),
    }
}

pub fn lbr(tree: &Tree, opponent: &dyn RangePolicy, config: &LbrConfig) -> LbrResult {
    summarize(tree, &[lbr_hands(tree, opponent, config)])
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::measure::fixtures::*;
    use crate::measure::policy::StrategyPolicy;

    fn slot(x: Card, y: Card) -> usize {
        let (a, b) = (usize::from(x.min(y)), usize::from(x.max(y)));
        a * (103 - a) / 2 + b - a - 1
    }

    impl Range {
        fn weight(&self, hole: [Card; 2]) -> f64 {
            self.weights[slot(hole[0], hole[1])]
        }

        fn live(&self) -> usize {
            self.weights.iter().filter(|&&w| w > 0.0).count()
        }
    }

    fn tree_child(tree: &Tree, node: NodeId, kind: ActionKind) -> NodeId {
        let Node::Decision { actions, .. } = tree.node(node) else {
            panic!("decision expected");
        };
        actions.iter().find(|(k, _)| *k == kind).unwrap().1
    }

    struct Passive<'a> {
        tree: &'a Tree,
        fold_to_bets: bool,
    }

    impl RangePolicy for Passive<'_> {
        fn probabilities(&self, node: NodeId, _hole: [Card; 2], _board: &[Card]) -> Vec<f32> {
            let Node::Decision {
                street, actions, ..
            } = self.tree.node(node)
            else {
                panic!("decision expected");
            };
            let folds = self.fold_to_bets && *street != Street::Preflop;
            let pick = actions
                .iter()
                .position(|(k, _)| folds && *k == ActionKind::Fold)
                .or_else(|| {
                    actions
                        .iter()
                        .position(|(k, _)| matches!(k, ActionKind::Check | ActionKind::Call))
                })
                .unwrap();
            (0..actions.len())
                .map(|i| if i == pick { 1.0 } else { 0.0 })
                .collect()
        }
    }

    struct ByCards;

    impl RangePolicy for ByCards {
        fn probabilities(&self, _node: NodeId, hole: [Card; 2], _board: &[Card]) -> Vec<f32> {
            if hole.contains(&0) {
                return vec![0.0, 1.0, 0.0];
            }
            let a = f32::from(hole[0] % 5 + 1);
            let b = f32::from(hole[1] % 3 + 1);
            let sum = a + b + 1.0;
            vec![a / sum, b / sum, 1.0 / sum]
        }
    }

    fn sum(range: &Range) -> f64 {
        range.weights.iter().sum()
    }

    fn config(hands: u64, seed: u64) -> LbrConfig {
        LbrConfig {
            flop_runouts: 20,
            ..LbrConfig::new(hands, seed, 400)
        }
    }

    #[test]
    fn config_defaults_to_200_flop_runouts() {
        let c = LbrConfig::new(10, 2, 2500);
        assert_eq!(
            (c.hands, c.seed, c.flop_runouts, c.stack),
            (10, 2, 200, 2500)
        );
    }

    #[test]
    fn every_hand_has_one_slot() {
        let mut seen = std::collections::HashSet::new();
        for (i, h) in HOLES.iter().enumerate() {
            assert!(h[0] < h[1] && h[1] < 52);
            assert_eq!(slot(h[0], h[1]), i);
            assert_eq!(slot(h[1], h[0]), i);
            assert!(seen.insert(*h));
        }
        assert_eq!(seen.len(), 1326);
    }

    #[test]
    fn range_starts_with_the_unblocked_hands() {
        let preflop = Range::new([51, 50], &[]);
        assert_eq!(preflop.weights.len(), 1326);
        assert_eq!(preflop.live(), 1326 - 101);
        assert!((sum(&preflop) - 1.0).abs() < 1e-12);
        assert_eq!(preflop.weight([51, 0]), 0.0);
        assert!((preflop.weight([0, 1]) - 1.0 / 1225.0).abs() < 1e-15);
        let flop = Range::new([51, 50], &[0, 1, 2]);
        assert_eq!(flop.live(), 1081);
        assert_eq!(flop.weight([0, 9]), 0.0);
        assert!((sum(&flop) - 1.0).abs() < 1e-12);
        let mut dealt = preflop.clone();
        dealt.block(&[0, 1, 2]);
        for (d, f) in dealt.weights.iter().zip(&flop.weights) {
            assert_eq!(*d == 0.0, *f == 0.0);
            assert!((d - f).abs() < 1e-15);
        }
    }

    #[test]
    fn weights_stay_normalised_and_follow_the_likelihood() {
        let tree = tiny_tree();
        let root = tree.root();
        let mut range = Range::new([51, 50], &[]);
        let mut expected = range.weights.clone();
        for action in [1usize, 2, 0, 1] {
            range.observe(&ByCards, root, action, &[]);
            assert!((sum(&range) - 1.0).abs() < 1e-9);
            for (h, w) in expected.iter_mut().enumerate() {
                *w *= f64::from(ByCards.probabilities(root, HOLES[h], &[])[action]);
            }
            let total: f64 = expected.iter().sum();
            for (h, w) in range.weights.iter().enumerate() {
                assert!((w - expected[h] / total).abs() < 1e-12);
            }
        }
    }

    #[test]
    fn an_impossible_action_removes_the_hand() {
        let tree = tiny_tree();
        let mut range = Range::new([51, 50], &[]);
        range.observe(&ByCards, tree.root(), 0, &[]);
        assert_eq!(range.weight([0, 7]), 0.0);
        assert_eq!(range.weight([0, 1]), 0.0);
        assert!(range.weight([4, 7]) > 0.0);
        assert_eq!(range.live(), 1225 - 49);
        assert!((sum(&range) - 1.0).abs() < 1e-12);
    }

    #[test]
    fn an_empty_range_falls_back_to_uniform() {
        let tree = tiny_tree();
        let mut range = Range::new([51, 50], &[]);
        range.observe(&ByCards, tree.root(), 0, &[]);
        let mut fresh = Range::new([51, 50], &[]);
        for (w, h) in fresh.weights.iter_mut().zip(&HOLES) {
            if !h.contains(&0) {
                *w = 0.0;
            }
        }
        fresh.observe(&ByCards, tree.root(), 0, &[]);
        assert_eq!(fresh.weights, Range::new([51, 50], &[]).weights);
    }

    #[test]
    fn strategy_rows_become_probabilities() {
        let file = trained();
        let policy = StrategyPolicy {
            file,
            abstraction: abstraction(),
        };
        let root = file.tree.root();
        let hole = [12, 33];
        let bucket = abstraction().bucket(Street::Preflop, hole, &[]);
        let row = file.probabilities(root, bucket);
        let p = RangePolicy::probabilities(&policy, root, hole, &[]);
        assert_eq!(p.len(), row.len());
        for (p, &r) in p.iter().zip(row) {
            assert_eq!(*p, f32::from(r) / 255.0);
        }
    }

    #[test]
    fn river_equity_is_exact() {
        let board = [0, 5, 22, 27, 44];
        let mut rng = Rng::new(1);
        let e = equities([51, 47], &board, 20, &mut rng);
        let mine = rank(&[51, 47, 0, 5, 22, 27, 44]);
        for (h, hole) in HOLES.iter().enumerate() {
            if hole
                .iter()
                .any(|c| [51, 47].contains(c) || board.contains(c))
            {
                continue;
            }
            let theirs = rank(&[hole[0], hole[1], 0, 5, 22, 27, 44]);
            let want = match mine.cmp(&theirs) {
                std::cmp::Ordering::Greater => 1.0,
                std::cmp::Ordering::Equal => 0.5,
                std::cmp::Ordering::Less => 0.0,
            };
            assert_eq!(e[h], want);
        }
    }

    #[test]
    fn turn_equity_counts_every_river() {
        let board = [0, 5, 22, 27];
        let mut rng = Rng::new(1);
        let e = equities([51, 47], &board, 20, &mut rng);
        let h = slot(8, 9);
        let mut score = 0.0;
        let mut n = 0.0;
        for r in 0..52u8 {
            if [51, 47, 0, 5, 22, 27, 8, 9].contains(&r) {
                continue;
            }
            let mine = rank(&[51, 47, 0, 5, 22, 27, r]);
            let theirs = rank(&[8, 9, 0, 5, 22, 27, r]);
            score += match mine.cmp(&theirs) {
                std::cmp::Ordering::Greater => 1.0,
                std::cmp::Ordering::Equal => 0.5,
                std::cmp::Ordering::Less => 0.0,
            };
            n += 1.0;
        }
        assert_eq!(n, 44.0);
        assert!((e[h] - score / n).abs() < 1e-12);
    }

    #[test]
    fn wager_value_follows_the_formula() {
        assert_eq!(call_value(0.25, 200, 50), 0.25 * 250.0 - 50.0);
        assert_eq!(wager_value(1.0, 0.3, 100, 100, 300), 100.0);
        let v = wager_value(0.4, 0.7, 100, 50, 200);
        assert!((v - (0.4 * 100.0 + 0.6 * (0.7 * 200.0 - 50.0))).abs() < 1e-12);
    }

    #[test]
    fn preflop_against_a_shove_lbr_calls_aces_and_folds_seven_deuce() {
        let tree = tiny_tree();
        let root = tree.root();
        let shove = tree_child(tree, root, ActionKind::AllIn);
        let committed = committed_at(tree, shove);
        let mut rng = Rng::new(2);
        let choose = |hole: [Card; 2], rng: &mut Rng| {
            let spot = Spot {
                tree,
                node: shove,
                me: 1,
                committed,
                stack: 400,
                board: &[],
            };
            let equity = equities(hole, &[], 200, rng);
            preflop_choice(&spot, &Range::new(hole, &[]), &equity)
        };
        let kind = |i: usize| match tree.node(shove) {
            Node::Decision { actions, .. } => actions[i].0,
            _ => unreachable!(),
        };
        assert_eq!(kind(choose([48, 49], &mut rng)), ActionKind::Call);
        assert_eq!(kind(choose([0, 21], &mut rng)), ActionKind::Fold);
    }

    #[test]
    fn preflop_equity_samples_whole_boards() {
        let mut rng = Rng::new(3);
        let e = equities([48, 49], &[], 200, &mut rng);
        let aces_vs_kings = e[slot(44, 45)];
        assert!((0.7..0.92).contains(&aces_vs_kings), "{aces_vs_kings}");
        assert_eq!(e[slot(48, 0)], 0.0);
    }

    #[test]
    fn raising_a_bet_values_the_real_pot_after_a_call() {
        assert_eq!(wager_value(0.0, 1.0, 120, 140, 2 * 180), 360.0 - 140.0);
    }

    #[test]
    fn hands_merge_into_the_same_result() {
        let tree = tiny_tree();
        let station = Passive {
            tree,
            fold_to_bets: false,
        };
        let hands = lbr_hands(tree, &station, &config(40, 9));
        assert_eq!(hands.len(), 40);
        assert!(hands.iter().enumerate().all(|(i, h)| h.0 == i % 2));
        let (a, b) = hands.split_at(17);
        let merged = summarize(tree, &[a.to_vec(), b.to_vec()]);
        assert_eq!(merged, lbr(tree, &station, &config(40, 9)));
    }

    #[test]
    fn lbr_bets_into_an_opponent_who_folds_to_bets_and_wins_the_pot() {
        let tree = tiny_tree();
        let folder = Passive {
            tree,
            fold_to_bets: true,
        };
        let hands = lbr_hands(tree, &folder, &config(300, 4));
        let limped = hands.iter().filter(|&&(seat, _)| seat == 0).count();
        let mut won = 0;
        for &(seat, chips) in &hands {
            match (seat, chips) {
                (_, 20) => won += 1,
                (0, -10) => {}
                other => panic!("unexpected hand {other:?}"),
            }
        }
        assert!(won >= hands.len() - limped / 10, "{won} of {}", hands.len());
        let result = summarize(tree, &[hands]);
        assert_eq!(result.by_seat[1], 1000.0);
    }

    #[test]
    fn lbr_beats_a_calling_station() {
        let tree = tiny_tree();
        let station = Passive {
            tree,
            fold_to_bets: false,
        };
        let result = lbr(tree, &station, &config(600, 5));
        assert!(result.mbb_per_hand - result.ci95 > 0.0, "{result:?}");
    }

    #[test]
    fn lbr_is_deterministic_for_a_seed() {
        let file = trained();
        let policy = StrategyPolicy {
            file,
            abstraction: abstraction(),
        };
        let a = lbr(&file.tree, &policy, &config(60, 7));
        let b = lbr(&file.tree, &policy, &config(60, 7));
        let c = lbr(&file.tree, &policy, &config(60, 8));
        assert_eq!(a, b);
        assert_ne!(a, c);
        assert!(a.mbb_per_hand.is_finite() && a.ci95 > 0.0);
    }
}
