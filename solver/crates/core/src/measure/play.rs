use crate::cards::Card;
use crate::eval::rank;
use crate::measure::policy::{Policy, SeatView};
use crate::measure::stats::{bb_per_100, Stats};
use crate::rng::Rng;
use crate::tree::{ActionKind, Node, NodeId, Street, Tree};

#[derive(Debug, Clone, Copy, PartialEq)]
pub struct MatchResult {
    pub bb_per_100: f64,
    pub ci95: f64,
    pub deals: u64,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct HandOutcome {
    pub node: NodeId,
    pub committed: [u32; 2],
    pub chips: [i64; 2],
}

pub(crate) fn visible(street: Street) -> usize {
    match street {
        Street::Preflop => 0,
        Street::Flop => 3,
        Street::Turn => 4,
        Street::River => 5,
    }
}

pub(crate) fn committed_at(tree: &Tree, node: NodeId) -> [u32; 2] {
    match tree.node(node) {
        Node::Decision { committed, .. }
        | Node::Fold { committed, .. }
        | Node::Showdown { committed } => *committed,
    }
}

pub(crate) fn deal(rng: &mut Rng) -> [Card; 9] {
    let mut deck: [Card; 52] = std::array::from_fn(|i| i as Card);
    rng.shuffle(&mut deck);
    deck[..9].try_into().expect("nine cards")
}

pub(crate) fn holes(deck: &[Card; 9]) -> [[Card; 2]; 2] {
    [[deck[1], deck[3]], [deck[0], deck[2]]]
}

pub(crate) fn start(tree: &Tree, stack: u32) -> [u32; 2] {
    committed_at(tree, tree.root()).map(|c| c.min(stack))
}

pub(crate) fn commit(
    tree: &Tree,
    kind: ActionKind,
    next: NodeId,
    committed: [u32; 2],
    me: usize,
    stack: u32,
) -> u32 {
    match kind {
        ActionKind::Fold | ActionKind::Check => committed[me],
        ActionKind::Call => committed[1 - me].min(stack),
        ActionKind::RaiseTo(_) => committed_at(tree, next)[me].min(stack),
        ActionKind::AllIn => stack,
    }
}

pub(crate) fn settle(
    tree: &Tree,
    node: NodeId,
    committed: [u32; 2],
    hole: &[[Card; 2]; 2],
    board: &[Card],
) -> Option<[i64; 2]> {
    match tree.node(node) {
        Node::Decision { .. } => None,
        Node::Fold { folder, .. } => {
            let loser = usize::from(*folder);
            let lost = i64::from(committed[loser]);
            let mut chips = [0i64; 2];
            chips[loser] = -lost;
            chips[1 - loser] = lost;
            Some(chips)
        }
        Node::Showdown { .. } => {
            let at_risk = i64::from(committed[0].min(committed[1]));
            let strength = |seat: usize| {
                let mut cards = [0 as Card; 7];
                cards[..2].copy_from_slice(&hole[seat]);
                cards[2..].copy_from_slice(board);
                rank(&cards)
            };
            Some(match strength(0).cmp(&strength(1)) {
                std::cmp::Ordering::Greater => [at_risk, -at_risk],
                std::cmp::Ordering::Less => [-at_risk, at_risk],
                std::cmp::Ordering::Equal => [0, 0],
            })
        }
    }
}

pub fn play_detailed(
    tree: &Tree,
    deck: &[Card; 9],
    policies: [&dyn Policy; 2],
    stack: u32,
    rng: &mut Rng,
) -> HandOutcome {
    let hole = holes(deck);
    let board = &deck[4..9];
    let mut node = tree.root();
    let mut committed = start(tree, stack);
    loop {
        if let Some(chips) = settle(tree, node, committed, &hole, board) {
            return HandOutcome {
                node,
                committed,
                chips,
            };
        }
        let Node::Decision {
            seat,
            street,
            actions,
            ..
        } = tree.node(node)
        else {
            unreachable!("settle returns None only for decisions");
        };
        let me = usize::from(*seat);
        let view = SeatView {
            hole: hole[me],
            board: board[..visible(*street)].to_vec(),
            street: *street,
        };
        let choice = policies[me].act(node, &view, rng);
        assert!(
            choice < actions.len(),
            "policy chose action {choice} of {} at node {node}",
            actions.len()
        );
        let (kind, next) = actions[choice];
        committed[me] = commit(tree, kind, next, committed, me, stack);
        node = next;
    }
}

pub fn play_hand(
    tree: &Tree,
    deck: &[Card; 9],
    policies: [&dyn Policy; 2],
    stack: u32,
    rng: &mut Rng,
) -> [i64; 2] {
    play_detailed(tree, deck, policies, stack, rng).chips
}

pub fn duplicate_match(
    tree: &Tree,
    a: &dyn Policy,
    b: &dyn Policy,
    stack: u32,
    deals: u64,
    seed: u64,
) -> MatchResult {
    let big_blind = committed_at(tree, tree.root())[1];
    let mut rng = Rng::new(seed);
    let mut stats = Stats::new();
    for _ in 0..deals {
        let nine = deal(&mut rng);
        let first = play_hand(tree, &nine, [a, b], stack, &mut rng);
        let second = play_hand(tree, &nine, [b, a], stack, &mut rng);
        stats.push((first[0] + second[1]) as f64);
    }
    MatchResult {
        bb_per_100: bb_per_100(stats.mean(), big_blind),
        ci95: bb_per_100(stats.half_width95(), big_blind),
        deals,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::measure::fixtures::*;
    use crate::measure::policy::{
        AlwaysCall, AlwaysRaise, Policy, RandomPolicy, SeatView, StrategyPolicy,
    };
    use crate::rng::Rng;
    use crate::tree::{ActionKind, Node, NodeId, Tree};

    struct Script<'a> {
        tree: &'a Tree,
        pick: fn(&[ActionKind]) -> usize,
    }

    impl Policy for Script<'_> {
        fn act(&self, node: NodeId, _view: &SeatView, _rng: &mut Rng) -> usize {
            let Node::Decision { actions, .. } = self.tree.node(node) else {
                panic!("decision expected");
            };
            let kinds: Vec<ActionKind> = actions.iter().map(|(k, _)| *k).collect();
            (self.pick)(&kinds)
        }
    }

    fn position(kinds: &[ActionKind], want: impl Fn(&ActionKind) -> bool) -> Option<usize> {
        kinds.iter().position(want)
    }

    fn shove(kinds: &[ActionKind]) -> usize {
        position(kinds, |k| *k == ActionKind::AllIn)
            .or_else(|| position(kinds, |k| *k == ActionKind::Call))
            .unwrap()
    }

    fn call(kinds: &[ActionKind]) -> usize {
        position(kinds, |k| matches!(k, ActionKind::Call | ActionKind::Check)).unwrap()
    }

    fn open(kinds: &[ActionKind]) -> usize {
        position(kinds, |k| matches!(k, ActionKind::RaiseTo(_))).unwrap()
    }

    fn fold(kinds: &[ActionKind]) -> usize {
        position(kinds, |k| *k == ActionKind::Fold).unwrap()
    }

    fn rigged() -> [u8; 9] {
        [20, 51, 1, 50, 10, 25, 39, 41, 14]
    }

    fn shuffled(rng: &mut Rng) -> [u8; 9] {
        let mut deck: Vec<u8> = (0..52).collect();
        rng.shuffle(&mut deck);
        deck[..9].try_into().unwrap()
    }

    #[test]
    fn hands_are_zero_sum() {
        let tree = tiny_tree();
        let (call, raise, random) = (
            AlwaysCall { tree },
            AlwaysRaise { tree },
            RandomPolicy { tree },
        );
        let all: [&dyn Policy; 3] = [&call, &raise, &random];
        let mut rng = Rng::new(5);
        for stack in [400, 600] {
            for i in 0..3_000 {
                let deck = shuffled(&mut rng);
                let p = [all[i % 3], all[(i / 3) % 3]];
                let chips = play_hand(tree, &deck, p, stack, &mut rng);
                assert_eq!(chips[0] + chips[1], 0);
            }
        }
    }

    #[test]
    fn real_commitments_equal_the_trees_at_the_preset_stack() {
        let tree = tiny_tree();
        let random = RandomPolicy { tree };
        let mut rng = Rng::new(6);
        for _ in 0..3_000 {
            let deck = shuffled(&mut rng);
            let out = play_detailed(tree, &deck, [&random, &random], 400, &mut rng);
            let tree_committed = match tree.node(out.node) {
                Node::Fold { committed, .. } | Node::Showdown { committed } => *committed,
                other => panic!("not terminal: {other:?}"),
            };
            assert_eq!(out.committed, tree_committed);
        }
    }

    #[test]
    fn preflop_all_in_and_call_pays_the_real_stack() {
        let tree = v1_tree();
        let (shover, caller) = (Script { tree, pick: shove }, Script { tree, pick: call });
        let mut rng = Rng::new(1);
        for stack in [2000u32, 2500, 3000] {
            let chips = play_hand(tree, &rigged(), [&shover, &caller], stack, &mut rng);
            assert_eq!(chips, [i64::from(stack), -i64::from(stack)]);
            let swapped = play_hand(tree, &rigged(), [&caller, &shover], stack, &mut rng);
            assert_eq!(swapped.iter().sum::<i64>(), 0);
        }
    }

    #[test]
    fn a_raise_and_fold_pays_the_same_at_any_stack() {
        let tree = v1_tree();
        let (raiser, folder) = (Script { tree, pick: open }, Script { tree, pick: fold });
        let mut rng = Rng::new(1);
        let at_2000 = play_hand(tree, &rigged(), [&raiser, &folder], 2000, &mut rng);
        let at_2500 = play_hand(tree, &rigged(), [&raiser, &folder], 2500, &mut rng);
        assert_eq!(at_2000, [20, -20]);
        assert_eq!(at_2500, at_2000);
    }

    #[test]
    fn the_better_hand_wins_the_pot_and_a_tie_splits() {
        let tree = tiny_tree();
        let (a, b) = (Script { tree, pick: call }, Script { tree, pick: call });
        let mut rng = Rng::new(1);
        let chips = play_hand(tree, &rigged(), [&a, &b], 400, &mut rng);
        assert_eq!(chips, [20, -20]);
        let swapped = [rigged()[1], rigged()[0], rigged()[3], rigged()[2]];
        let mut deck = rigged();
        deck[..4].copy_from_slice(&swapped);
        let chips = play_hand(tree, &deck, [&a, &b], 400, &mut rng);
        assert_eq!(chips, [-20, 20]);
        let board_plays = [0, 1, 4, 5, 51, 47, 43, 39, 35];
        let chips = play_hand(tree, &board_plays, [&a, &b], 400, &mut rng);
        assert_eq!(chips, [0, 0]);
    }

    #[test]
    fn identical_deterministic_policies_cancel_exactly() {
        let tree = tiny_tree();
        let call = AlwaysCall { tree };
        let result = duplicate_match(tree, &call, &call, 400, 2_000, 9);
        assert_eq!(result.bb_per_100, 0.0);
        assert_eq!(result.ci95, 0.0);
        assert_eq!(result.deals, 2_000);
    }

    #[test]
    fn random_against_itself_covers_zero() {
        let tree = tiny_tree();
        let random = RandomPolicy { tree };
        let result = duplicate_match(tree, &random, &random, 400, 4_000, 3);
        assert!(result.ci95 > 0.0);
        assert!(result.bb_per_100.abs() <= result.ci95, "{result:?}");
    }

    #[test]
    fn match_is_reproducible_from_its_seed() {
        let tree = tiny_tree();
        let (raise, random) = (AlwaysRaise { tree }, RandomPolicy { tree });
        let a = duplicate_match(tree, &raise, &random, 400, 500, 4);
        let b = duplicate_match(tree, &raise, &random, 400, 500, 4);
        assert_eq!((a.bb_per_100, a.ci95), (b.bb_per_100, b.ci95));
    }

    #[test]
    fn swapping_the_sides_negates_the_result() {
        let tree = tiny_tree();
        let (raise, random) = (AlwaysRaise { tree }, AlwaysCall { tree });
        let a = duplicate_match(tree, &raise, &random, 400, 1_000, 4);
        let b = duplicate_match(tree, &random, &raise, 400, 1_000, 4);
        assert!((a.bb_per_100 + b.bb_per_100).abs() < 1e-9);
        assert!((a.ci95 - b.ci95).abs() < 1e-9);
    }

    #[test]
    fn trained_strategy_beats_random() {
        let file = trained();
        let policy = StrategyPolicy {
            file,
            abstraction: abstraction(),
        };
        let random = RandomPolicy { tree: &file.tree };
        let result = duplicate_match(&file.tree, &policy, &random, 400, 20_000, 11);
        assert!(result.bb_per_100 - result.ci95 > 0.0, "{result:?}");
    }
}
