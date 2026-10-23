use crate::abstraction::Abstraction;
use crate::cards::Card;
use crate::rng::Rng;
use crate::strategy::StrategyFile;
use crate::tree::{ActionKind, Node, NodeId, Street, Tree};

#[derive(Debug, Clone)]
pub struct SeatView {
    pub hole: [Card; 2],
    pub board: Vec<Card>,
    pub street: Street,
}

pub trait Policy {
    fn act(&self, node: NodeId, seat_view: &SeatView, rng: &mut Rng) -> usize;
}

fn kinds(tree: &Tree, node: NodeId) -> impl Iterator<Item = ActionKind> + '_ {
    let Node::Decision { actions, .. } = tree.node(node) else {
        panic!("node {node} is not a decision");
    };
    actions.iter().map(|(kind, _)| *kind)
}

fn passive(tree: &Tree, node: NodeId) -> usize {
    kinds(tree, node)
        .position(|k| matches!(k, ActionKind::Check | ActionKind::Call))
        .unwrap_or(0)
}

pub struct AlwaysCall<'a> {
    pub tree: &'a Tree,
}

impl Policy for AlwaysCall<'_> {
    fn act(&self, node: NodeId, _seat_view: &SeatView, _rng: &mut Rng) -> usize {
        passive(self.tree, node)
    }
}

pub struct AlwaysRaise<'a> {
    pub tree: &'a Tree,
}

impl Policy for AlwaysRaise<'_> {
    fn act(&self, node: NodeId, _seat_view: &SeatView, _rng: &mut Rng) -> usize {
        let size = |k: ActionKind| match k {
            ActionKind::RaiseTo(x) => Some((0, x)),
            ActionKind::AllIn => Some((1, 0)),
            _ => None,
        };
        kinds(self.tree, node)
            .enumerate()
            .filter_map(|(i, k)| size(k).map(|s| (s, i)))
            .max()
            .map_or_else(|| passive(self.tree, node), |(_, i)| i)
    }
}

pub struct RandomPolicy<'a> {
    pub tree: &'a Tree,
}

impl Policy for RandomPolicy<'_> {
    fn act(&self, node: NodeId, _seat_view: &SeatView, rng: &mut Rng) -> usize {
        rng.below(kinds(self.tree, node).count() as u32) as usize
    }
}

pub struct StrategyPolicy<'a> {
    pub file: &'a StrategyFile,
    pub abstraction: &'a Abstraction,
}

impl Policy for StrategyPolicy<'_> {
    fn act(&self, node: NodeId, seat_view: &SeatView, rng: &mut Rng) -> usize {
        let bucket = self
            .abstraction
            .bucket(seat_view.street, seat_view.hole, &seat_view.board);
        let probabilities = self.file.probabilities(node, bucket);
        let mut draw = rng.below(255);
        for (i, &p) in probabilities.iter().enumerate() {
            let p = u32::from(p);
            if draw < p {
                return i;
            }
            draw -= p;
        }
        probabilities
            .iter()
            .rposition(|&p| p > 0)
            .unwrap_or(probabilities.len() - 1)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::measure::fixtures::*;
    use crate::tree::{ActionKind, Node};

    fn view(street: Street) -> SeatView {
        SeatView {
            hole: [51, 50],
            board: Vec::new(),
            street,
        }
    }

    fn kinds(tree: &Tree, node: NodeId) -> Vec<ActionKind> {
        let Node::Decision { actions, .. } = tree.node(node) else {
            panic!("decision expected");
        };
        actions.iter().map(|(k, _)| *k).collect()
    }

    fn decisions(tree: &Tree) -> Vec<NodeId> {
        (0..tree.nodes().len() as NodeId)
            .filter(|&id| matches!(tree.node(id), Node::Decision { .. }))
            .collect()
    }

    #[test]
    fn always_call_checks_or_calls() {
        let tree = tiny_tree();
        let policy = AlwaysCall { tree };
        let mut rng = Rng::new(1);
        for id in decisions(tree) {
            let k = kinds(tree, id);
            let a = policy.act(id, &view(Street::Preflop), &mut rng);
            assert!(matches!(k[a], ActionKind::Check | ActionKind::Call));
        }
    }

    #[test]
    fn always_raise_takes_the_largest_aggression() {
        let tree = tiny_tree();
        let policy = AlwaysRaise { tree };
        let mut rng = Rng::new(1);
        let mut raised = 0;
        for id in decisions(tree) {
            let k = kinds(tree, id);
            let a = policy.act(id, &view(Street::Preflop), &mut rng);
            let aggressive =
                |k: &ActionKind| matches!(k, ActionKind::RaiseTo(_) | ActionKind::AllIn);
            if k.iter().any(aggressive) {
                raised += 1;
                assert!(aggressive(&k[a]));
                let rank = |k: &ActionKind| match k {
                    ActionKind::RaiseTo(x) => (0, *x),
                    ActionKind::AllIn => (1, 0),
                    _ => (0, 0),
                };
                assert!(k.iter().all(|other| rank(other) <= rank(&k[a])));
            } else {
                assert!(matches!(k[a], ActionKind::Check | ActionKind::Call));
            }
        }
        assert!(raised > 0);
    }

    #[test]
    fn random_policy_reaches_every_action() {
        let tree = tiny_tree();
        let policy = RandomPolicy { tree };
        let mut rng = Rng::new(2);
        let root = tree.root();
        let n = kinds(tree, root).len();
        let mut seen = vec![0u32; n];
        for _ in 0..4_000 {
            seen[policy.act(root, &view(Street::Preflop), &mut rng)] += 1;
        }
        assert!(seen.iter().all(|&c| c > 4_000 / n as u32 / 2));
    }

    #[test]
    fn strategy_policy_samples_the_stored_distribution() {
        let file = trained();
        let policy = StrategyPolicy {
            file,
            abstraction: abstraction(),
        };
        let root = file.tree.root();
        let view = view(Street::Preflop);
        let bucket = abstraction().bucket(Street::Preflop, view.hole, &[]);
        let stored = file.probabilities(root, bucket);
        let mut rng = Rng::new(3);
        let draws = 20_000u32;
        let mut seen = vec![0u32; stored.len()];
        for _ in 0..draws {
            seen[policy.act(root, &view, &mut rng)] += 1;
        }
        for (count, &p) in seen.iter().zip(stored) {
            let expected = f64::from(p) / 255.0;
            let got = f64::from(*count) / f64::from(draws);
            assert!((got - expected).abs() < 0.02, "{got} vs {expected}");
        }
    }

    #[test]
    fn strategy_policy_never_picks_a_zero_probability_action() {
        let file = trained();
        let policy = StrategyPolicy {
            file,
            abstraction: abstraction(),
        };
        let mut rng = Rng::new(4);
        for id in decisions(&file.tree).into_iter().take(40) {
            let Node::Decision { street, .. } = file.tree.node(id) else {
                unreachable!()
            };
            let board: Vec<u8> = [3u8, 10, 25, 30, 41][..match street {
                Street::Preflop => 0,
                Street::Flop => 3,
                Street::Turn => 4,
                Street::River => 5,
            }]
                .to_vec();
            let v = SeatView {
                hole: [51, 50],
                board,
                street: *street,
            };
            let bucket = abstraction().bucket(*street, v.hole, &v.board);
            let stored = file.probabilities(id, bucket);
            for _ in 0..200 {
                let a = policy.act(id, &v, &mut rng);
                assert!(stored[a] > 0);
            }
        }
    }
}
