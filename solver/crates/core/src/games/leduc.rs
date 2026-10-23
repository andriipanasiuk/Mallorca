use std::cmp::Ordering;

use crate::cfr::Game;
use crate::games::exact::ExactGame;
use crate::rng::Rng;

pub struct LeducPoker;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
enum Kind {
    Fold,
    Check,
    Call,
    Raise,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
enum Status {
    Playing,
    Folded(u8),
    Showdown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct LeducState {
    cards: [u8; 2],
    board: u8,
    round: u8,
    node: u8,
    first_round: u8,
    contrib: [u32; 2],
    status: Status,
}

const DECK: u8 = 6;
const NO_BOARD: u8 = u8::MAX;
const ANTE: u32 = 1;
const RAISE: [u32; 2] = [2, 4];
const NODES: u64 = 6;
const FIRST_ROUND_ENDINGS: u64 = 5;
const SLOTS: u64 = 3;
const ACTOR: [u8; 6] = [0, 1, 1, 0, 0, 1];
const ACTIONS: [&[Kind]; 6] = [
    &[Kind::Check, Kind::Raise],
    &[Kind::Check, Kind::Raise],
    &[Kind::Fold, Kind::Call, Kind::Raise],
    &[Kind::Fold, Kind::Call, Kind::Raise],
    &[Kind::Fold, Kind::Call],
    &[Kind::Fold, Kind::Call],
];

enum Step {
    Node(u8),
    RoundEnd(u8),
}

fn step(node: u8, kind: Kind) -> Step {
    match (node, kind) {
        (0, Kind::Check) => Step::Node(1),
        (0, Kind::Raise) => Step::Node(2),
        (1, Kind::Check) => Step::RoundEnd(0),
        (1, Kind::Raise) => Step::Node(3),
        (2, Kind::Call) => Step::RoundEnd(1),
        (2, Kind::Raise) => Step::Node(4),
        (3, Kind::Call) => Step::RoundEnd(2),
        (3, Kind::Raise) => Step::Node(5),
        (4, Kind::Call) => Step::RoundEnd(3),
        (5, Kind::Call) => Step::RoundEnd(4),
        _ => panic!("no {kind:?} at node {node}"),
    }
}

fn rank(card: u8) -> u8 {
    card / 2
}

fn strength(card: u8, board: u8) -> u8 {
    if rank(card) == rank(board) {
        DECK + rank(card)
    } else {
        rank(card)
    }
}

impl LeducPoker {
    pub fn deal(&self, cards: [u8; 2]) -> LeducState {
        LeducState {
            cards,
            board: NO_BOARD,
            round: 0,
            node: 0,
            first_round: 0,
            contrib: [ANTE; 2],
            status: Status::Playing,
        }
    }

    fn act(&self, s: &LeducState, action: usize) -> LeducState {
        let mut n = *s;
        let p = ACTOR[s.node as usize] as usize;
        let kind = ACTIONS[s.node as usize][action];
        match kind {
            Kind::Fold => {
                n.status = Status::Folded(p as u8);
                return n;
            }
            Kind::Check => {}
            Kind::Call => n.contrib[p] = n.contrib[1 - p],
            Kind::Raise => n.contrib[p] = n.contrib[1 - p] + RAISE[s.round as usize],
        }
        match step(s.node, kind) {
            Step::Node(next) => n.node = next,
            Step::RoundEnd(ending) => {
                if s.round == 0 {
                    n.round = 1;
                    n.node = 0;
                    n.first_round = ending;
                } else {
                    n.status = Status::Showdown;
                }
            }
        }
        n
    }

    fn needs_board(&self, s: &LeducState) -> bool {
        s.status == Status::Playing && s.round == 1 && s.board == NO_BOARD
    }

    fn remaining(&self, s: &LeducState) -> impl Iterator<Item = u8> {
        let cards = s.cards;
        (0..DECK).filter(move |c| !cards.contains(c))
    }
}

impl Game for LeducPoker {
    type State = LeducState;

    fn root(&self, rng: &mut Rng) -> LeducState {
        let first = rng.below(u32::from(DECK)) as u8;
        let mut second = rng.below(u32::from(DECK) - 1) as u8;
        if second >= first {
            second += 1;
        }
        self.deal([first, second])
    }

    fn is_terminal(&self, s: &LeducState) -> bool {
        s.status != Status::Playing
    }

    fn utility(&self, s: &LeducState, player: u8) -> f64 {
        let p = player as usize;
        let outcome = match s.status {
            Status::Folded(loser) if loser == player => Ordering::Less,
            Status::Folded(_) => Ordering::Greater,
            Status::Showdown => {
                strength(s.cards[p], s.board).cmp(&strength(s.cards[1 - p], s.board))
            }
            Status::Playing => panic!("state is not terminal"),
        };
        match outcome {
            Ordering::Less => -f64::from(s.contrib[p]),
            Ordering::Equal => 0.0,
            Ordering::Greater => f64::from(s.contrib[1 - p]),
        }
    }

    fn player(&self, s: &LeducState) -> u8 {
        ACTOR[s.node as usize]
    }

    fn num_actions(&self, s: &LeducState) -> usize {
        ACTIONS[s.node as usize].len()
    }

    fn infoset_offset(&self, s: &LeducState) -> u64 {
        let own = u64::from(rank(s.cards[self.player(s) as usize]));
        let node = u64::from(s.node);
        let index = if s.round == 0 {
            node * 3 + own
        } else {
            let board = u64::from(rank(s.board));
            NODES * 3 + ((u64::from(s.first_round) * NODES + node) * 3 + board) * 3 + own
        };
        index * SLOTS
    }

    fn apply(&self, s: &LeducState, action: usize, rng: &mut Rng) -> LeducState {
        let mut n = self.act(s, action);
        if self.needs_board(&n) {
            let pick = rng.below(u32::from(DECK) - 2) as usize;
            n.board = self.remaining(&n).nth(pick).unwrap();
        }
        n
    }

    fn infoset_len(&self) -> u64 {
        (NODES * 3 + FIRST_ROUND_ENDINGS * NODES * 3 * 3) * SLOTS
    }
}

impl ExactGame for LeducPoker {
    fn deals(&self) -> Vec<(f64, LeducState)> {
        let mut out = Vec::new();
        for a in 0..DECK {
            for b in 0..DECK {
                if a != b {
                    out.push((1.0 / 30.0, self.deal([a, b])));
                }
            }
        }
        out
    }

    fn outcomes(&self, s: &LeducState, action: usize) -> Vec<(f64, LeducState)> {
        let n = self.act(s, action);
        if !self.needs_board(&n) {
            return vec![(1.0, n)];
        }
        let p = 1.0 / f64::from(DECK - 2);
        self.remaining(&n)
            .map(|board| (p, LeducState { board, ..n }))
            .collect()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cfr::Storage;
    use crate::games::{exploitability, train};
    use crate::preset::CfrParams;
    use std::collections::HashSet;

    const JS: u8 = 0;
    const JH: u8 = 1;
    const QS: u8 = 2;
    const KS: u8 = 4;

    fn play(cards: [u8; 2], board: u8, actions: &[usize]) -> LeducState {
        let g = LeducPoker;
        let mut s = g.deal(cards);
        for &a in actions {
            s = g.act(&s, a);
            if g.needs_board(&s) {
                s.board = board;
            }
        }
        s
    }

    #[test]
    fn folds_and_showdowns_pay_the_right_amounts() {
        let g = LeducPoker;
        let s = play([JS, KS], QS, &[1, 0]);
        assert!(g.is_terminal(&s));
        assert_eq!((g.utility(&s, 0), g.utility(&s, 1)), (1.0, -1.0));
        let s = play([JS, KS], QS, &[1, 2, 1, 0, 0]);
        assert!(g.is_terminal(&s));
        assert_eq!((g.utility(&s, 0), g.utility(&s, 1)), (-5.0, 5.0));
        let s = play([JS, KS], JH, &[0, 1, 1, 1, 2, 1]);
        assert!(g.is_terminal(&s));
        assert_eq!((g.utility(&s, 0), g.utility(&s, 1)), (11.0, -11.0));
        let s = play([JS, JH], KS, &[0, 0, 0, 0]);
        assert!(g.is_terminal(&s));
        assert_eq!((g.utility(&s, 0), g.utility(&s, 1)), (0.0, 0.0));
    }

    #[test]
    fn raises_are_capped_at_two_per_round() {
        let g = LeducPoker;
        let s = play([JS, KS], QS, &[1, 2]);
        assert_eq!(g.num_actions(&s), 2);
        assert_eq!(s.contrib, [3, 5]);
        let s = play([JS, KS], QS, &[0, 1, 2, 1]);
        assert_eq!(s.round, 1);
        assert_eq!(s.contrib, [5, 5]);
        assert_eq!(g.num_actions(&s), 2);
        let s = play([JS, KS], QS, &[0, 1, 2, 1, 1, 2]);
        assert_eq!(s.contrib, [9, 13]);
        assert_eq!(g.num_actions(&s), 2);
    }

    #[test]
    fn offsets_are_distinct_and_in_range() {
        let g = LeducPoker;
        let mut seen = HashSet::new();
        let mut stack: Vec<LeducState> = g.deals().into_iter().map(|d| d.1).collect();
        while let Some(s) = stack.pop() {
            if g.is_terminal(&s) {
                continue;
            }
            let o = g.infoset_offset(&s);
            assert!(o + g.num_actions(&s) as u64 <= g.infoset_len());
            seen.insert(o);
            for a in 0..g.num_actions(&s) {
                stack.extend(g.outcomes(&s, a).into_iter().map(|o| o.1));
            }
        }
        assert_eq!(seen.len(), 288);
        assert!(seen.iter().all(|o| o % SLOTS == 0));
    }

    #[test]
    fn sampled_board_avoids_private_cards() {
        let g = LeducPoker;
        let mut rng = Rng::new(5);
        for _ in 0..1000 {
            let root = g.root(&mut rng);
            let s = g.apply(&g.apply(&root, 0, &mut rng), 0, &mut rng);
            assert_eq!(s.round, 1);
            assert!(s.board < DECK && !s.cards.contains(&s.board));
            assert_ne!(s.cards[0], s.cards[1]);
        }
    }

    #[test]
    fn uniform_profile_exploitability() {
        let g = LeducPoker;
        let s = Storage::new(g.infoset_len());
        let e = exploitability(&g, &s);
        assert!((e - 4.747222222222222 / 2.0).abs() < 1e-6, "{e}");
    }

    #[test]
    #[ignore]
    fn dcfr_converges_on_leduc() {
        let g = LeducPoker;
        let params = CfrParams {
            alpha: 1.5,
            beta: 0.0,
            gamma: 2.0,
            discount_interval: 1000,
        };
        let s = train(&g, 16_000_000, &params, 1);
        let e = exploitability(&g, &s);
        assert!(e < 0.01, "exploitability {e}");
    }
}
