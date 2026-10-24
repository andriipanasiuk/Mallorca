use crate::abstraction::Abstraction;
use crate::cards::Card;
use crate::cfr::Game;
use crate::eval::rank;
use crate::rng::Rng;
use crate::tree::{Node, NodeId, Street, Tree};

pub struct Holdem<'a> {
    tree: &'a Tree,
    abstraction: &'a Abstraction,
}

#[derive(Debug, Clone)]
pub struct HoldemState {
    node: NodeId,
    hole: [[Card; 2]; 2],
    board: [Card; 5],
    buckets: [[u16; 4]; 2],
    ranks: [u16; 2],
}

impl HoldemState {
    pub fn hole(&self) -> [[Card; 2]; 2] {
        self.hole
    }

    pub fn board(&self) -> [Card; 5] {
        self.board
    }
}

fn showdown_ranks(hole: &[[Card; 2]; 2], board: &[Card; 5]) -> [u16; 2] {
    hole.map(|h| {
        let mut cards = [0 as Card; 7];
        cards[..2].copy_from_slice(&h);
        cards[2..].copy_from_slice(board);
        rank(&cards)
    })
}

impl<'a> Holdem<'a> {
    pub fn new(tree: &'a Tree, abstraction: &'a Abstraction) -> Holdem<'a> {
        Holdem { tree, abstraction }
    }

    fn deal_buckets(&self, hole: &[[Card; 2]; 2], board: &[Card; 5]) -> [[u16; 4]; 2] {
        let streets = [
            (Street::Preflop, 0),
            (Street::Flop, 3),
            (Street::Turn, 4),
            (Street::River, 5),
        ];
        let mut out = [[0u16; 4]; 2];
        for (seat, row) in out.iter_mut().enumerate() {
            for (street, seen) in streets {
                row[street.as_index()] =
                    self.abstraction.bucket(street, hole[seat], &board[..seen]);
            }
        }
        out
    }

    fn decision(&self, s: &HoldemState) -> (u8, Street, usize) {
        match self.tree.node(s.node) {
            Node::Decision {
                seat,
                street,
                actions,
                ..
            } => (*seat, *street, actions.len()),
            other => panic!("node {} is not a decision: {other:?}", s.node),
        }
    }
}

impl Game for Holdem<'_> {
    type State = HoldemState;

    fn root(&self, rng: &mut Rng) -> HoldemState {
        let mut deck: [Card; 52] = std::array::from_fn(|i| i as Card);
        rng.shuffle(&mut deck);
        let hole = [[deck[1], deck[3]], [deck[0], deck[2]]];
        let board = [deck[4], deck[5], deck[6], deck[7], deck[8]];
        HoldemState {
            node: self.tree.root(),
            hole,
            board,
            buckets: self.deal_buckets(&hole, &board),
            ranks: showdown_ranks(&hole, &board),
        }
    }

    fn is_terminal(&self, s: &HoldemState) -> bool {
        !matches!(self.tree.node(s.node), Node::Decision { .. })
    }

    fn utility(&self, s: &HoldemState, player: u8) -> f64 {
        let me = player as usize;
        match self.tree.node(s.node) {
            Node::Fold { folder, committed } => {
                let lost = f64::from(committed[*folder as usize]);
                if *folder == player {
                    -lost
                } else {
                    lost
                }
            }
            Node::Showdown { committed } => {
                let at_risk = f64::from(committed[0].min(committed[1]));
                match s.ranks[me].cmp(&s.ranks[1 - me]) {
                    std::cmp::Ordering::Greater => at_risk,
                    std::cmp::Ordering::Less => -at_risk,
                    std::cmp::Ordering::Equal => 0.0,
                }
            }
            other => panic!("node {} is not terminal: {other:?}", s.node),
        }
    }

    fn player(&self, s: &HoldemState) -> u8 {
        self.decision(s).0
    }

    fn num_actions(&self, s: &HoldemState) -> usize {
        self.decision(s).2
    }

    fn infoset_offset(&self, s: &HoldemState) -> u64 {
        let (seat, street, _) = self.decision(s);
        self.tree
            .offset(s.node, s.buckets[seat as usize][street.as_index()])
    }

    fn apply(&self, s: &HoldemState, action: usize, _rng: &mut Rng) -> HoldemState {
        let Node::Decision { actions, .. } = self.tree.node(s.node) else {
            panic!("node {} is not a decision", s.node);
        };
        HoldemState {
            node: actions[action].1,
            ..s.clone()
        }
    }

    fn infoset_len(&self) -> u64 {
        self.tree.action_slot_count()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cards::parse_cards;
    use crate::cfr::{iterate, Storage};
    use crate::preset::Preset;
    use std::sync::OnceLock;

    const TINY: &str = include_str!("../../../presets/tiny.toml");

    fn synthetic() -> &'static Abstraction {
        static A: OnceLock<Abstraction> = OnceLock::new();
        A.get_or_init(|| {
            let table = |n: usize| (0..n).map(|i| (i % 8) as u8).collect::<Vec<u8>>();
            Abstraction::new([table(1_286_792), table(13_960_050), table(123_156_254)])
        })
    }

    fn tiny_tree() -> Tree {
        Tree::build(&Preset::from_toml(TINY).unwrap())
    }

    fn find(tree: &Tree, pred: impl Fn(&Node) -> bool) -> NodeId {
        tree.nodes().iter().position(pred).expect("node") as NodeId
    }

    fn state_at(node: NodeId, hole: [[Card; 2]; 2], board: [Card; 5]) -> HoldemState {
        HoldemState {
            node,
            hole,
            board,
            buckets: [[0; 4]; 2],
            ranks: showdown_ranks(&hole, &board),
        }
    }

    fn cards(s: &str) -> Vec<Card> {
        parse_cards(s).unwrap()
    }

    fn rollout(game: &Holdem, rng: &mut Rng) -> HoldemState {
        let mut s = game.root(rng);
        while !game.is_terminal(&s) {
            let a = rng.below(game.num_actions(&s) as u32) as usize;
            s = game.apply(&s, a, rng);
        }
        s
    }

    #[test]
    fn utilities_are_zero_sum() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let mut rng = Rng::new(7);
        for _ in 0..10_000 {
            let s = rollout(&game, &mut rng);
            assert_eq!(game.utility(&s, 0) + game.utility(&s, 1), 0.0);
        }
    }

    #[test]
    fn button_fold_costs_ten() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let mut rng = Rng::new(1);
        let root = game.root(&mut rng);
        assert_eq!(game.player(&root), 0);
        let fold = find(&tree, |n| {
            matches!(
                n,
                Node::Fold {
                    folder: 0,
                    committed: [10, 20]
                }
            )
        });
        let s = state_at(fold, root.hole, root.board);
        assert!(game.is_terminal(&s));
        assert_eq!(game.utility(&s, 0), -10.0);
        assert_eq!(game.utility(&s, 1), 10.0);
    }

    #[test]
    fn showdown_pays_better_hand() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let node = find(&tree, |n| {
            matches!(
                n,
                Node::Showdown {
                    committed: [400, 400]
                }
            )
        });
        let aces = cards("As Ad");
        let kings = cards("Kc Kd");
        let board: [Card; 5] = cards("2c 7d 9h Js 3c").try_into().unwrap();
        let s = state_at(node, [[aces[0], aces[1]], [kings[0], kings[1]]], board);
        assert_eq!(game.utility(&s, 0), 400.0);
        assert_eq!(game.utility(&s, 1), -400.0);
        let s = state_at(node, [[kings[0], kings[1]], [aces[0], aces[1]]], board);
        assert_eq!(game.utility(&s, 0), -400.0);
        assert_eq!(game.utility(&s, 1), 400.0);
    }

    #[test]
    fn tied_showdown_splits() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let node = find(&tree, |n| matches!(n, Node::Showdown { .. }));
        let a = cards("2c 3d");
        let b = cards("2d 3c");
        let board: [Card; 5] = cards("Ah Kh Qh Jh Th").try_into().unwrap();
        let s = state_at(node, [[a[0], a[1]], [b[0], b[1]]], board);
        assert_eq!(game.utility(&s, 0), 0.0);
        assert_eq!(game.utility(&s, 1), 0.0);
    }

    #[test]
    fn all_in_preflop_reaches_showdown_with_full_board() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let mut rng = Rng::new(3);
        let mut s = game.root(&mut rng);
        let mut seen = std::collections::HashSet::new();
        for c in s.hole.iter().flatten().chain(s.board.iter()) {
            assert!(seen.insert(*c));
        }
        assert_eq!(seen.len(), 9);
        while let Node::Decision { actions, .. } = tree.node(s.node) {
            let a = actions.len() - 1;
            s = game.apply(&s, a, &mut rng);
        }
        assert!(matches!(
            tree.node(s.node),
            Node::Showdown {
                committed: [400, 400]
            }
        ));
        assert!(game.is_terminal(&s));
    }

    #[test]
    fn deal_order_matches_engine() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let mut a = Rng::new(11);
        let mut deck: Vec<Card> = (0..52).collect();
        a.shuffle(&mut deck);
        let mut b = Rng::new(11);
        let s = game.root(&mut b);
        assert_eq!(s.hole[1], [deck[0], deck[2]]);
        assert_eq!(s.hole[0], [deck[1], deck[3]]);
        assert_eq!(s.board, [deck[4], deck[5], deck[6], deck[7], deck[8]]);
    }

    #[test]
    fn infoset_offset_uses_acting_seat_bucket() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let mut rng = Rng::new(5);
        for _ in 0..200 {
            let mut s = game.root(&mut rng);
            while !game.is_terminal(&s) {
                let Node::Decision { seat, street, .. } = tree.node(s.node) else {
                    unreachable!()
                };
                let bucket = s.buckets[*seat as usize][street.as_index()];
                assert_eq!(game.infoset_offset(&s), tree.offset(s.node, bucket));
                let a = rng.below(game.num_actions(&s) as u32) as usize;
                s = game.apply(&s, a, &mut rng);
            }
        }
        assert_eq!(game.infoset_len(), tree.action_slot_count());
    }

    #[test]
    fn root_holds_both_showdown_ranks() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let mut rng = Rng::new(13);
        for _ in 0..1_000 {
            let s = game.root(&mut rng);
            for seat in 0..2 {
                let mut seven = s.hole[seat].to_vec();
                seven.extend_from_slice(&s.board);
                assert_eq!(s.ranks[seat], rank(&seven));
            }
        }
    }

    #[test]
    fn buckets_follow_abstraction() {
        let tree = tiny_tree();
        let abs = synthetic();
        let game = Holdem::new(&tree, abs);
        let mut rng = Rng::new(9);
        let s = game.root(&mut rng);
        for seat in 0..2 {
            assert_eq!(
                s.buckets[seat][0],
                abs.bucket(Street::Preflop, s.hole[seat], &[])
            );
            assert_eq!(
                s.buckets[seat][1],
                abs.bucket(Street::Flop, s.hole[seat], &s.board[..3])
            );
            assert_eq!(
                s.buckets[seat][2],
                abs.bucket(Street::Turn, s.hole[seat], &s.board[..4])
            );
            assert_eq!(
                s.buckets[seat][3],
                abs.bucket(Street::River, s.hole[seat], &s.board)
            );
        }
    }

    #[test]
    fn training_tiny_leaves_no_nan() {
        let tree = tiny_tree();
        let game = Holdem::new(&tree, synthetic());
        let storage = Storage::new(game.infoset_len());
        let mut rng = Rng::new(21);
        for i in 0..2_000u64 {
            iterate(&game, &storage, (i % 2) as u8, &mut rng);
        }
        let mut nonzero = false;
        for i in 0..storage.len() {
            let r = storage.regret(i);
            assert!(!r.is_nan() && !storage.strategy_sum(i).is_nan());
            nonzero |= r != 0.0;
        }
        assert!(nonzero);
    }
}
