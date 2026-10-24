use std::collections::HashMap;

use crate::cfr::{Game, Storage};

pub trait ExactGame: Game {
    fn deals(&self) -> Vec<(f64, Self::State)>;
    fn outcomes(&self, s: &Self::State, action: usize) -> Vec<(f64, Self::State)>;
}

struct Infoset<S> {
    depth: usize,
    histories: Vec<(f64, S)>,
}

struct BestResponse<'a, G: ExactGame> {
    game: &'a G,
    storage: &'a Storage,
    player: u8,
    infosets: HashMap<u64, Infoset<G::State>>,
    policy: HashMap<u64, usize>,
}

impl<G: ExactGame> BestResponse<'_, G> {
    fn collect(&mut self, s: &G::State, weight: f64, depth: usize) {
        if self.game.is_terminal(s) {
            return;
        }
        let n = self.game.num_actions(s);
        let key = self.game.infoset_offset(s);
        let probs = if self.game.player(s) == self.player {
            let infoset = self.infosets.entry(key).or_insert(Infoset {
                depth,
                histories: Vec::new(),
            });
            assert_eq!(infoset.depth, depth, "infoset {key} spans depths");
            infoset.histories.push((weight, s.clone()));
            vec![1.0; n]
        } else {
            self.opponent(key, n)
        };
        for (a, p) in probs.into_iter().enumerate() {
            if p > 0.0 {
                for (q, child) in self.game.outcomes(s, a) {
                    self.collect(&child, weight * p * q, depth + 1);
                }
            }
        }
    }

    fn opponent(&self, key: u64, n: usize) -> Vec<f64> {
        self.storage
            .average_strategy(key, n)
            .into_iter()
            .map(f64::from)
            .collect()
    }

    fn after(&self, s: &G::State, action: usize) -> f64 {
        self.game
            .outcomes(s, action)
            .iter()
            .map(|(q, child)| q * self.value(child))
            .sum()
    }

    fn value(&self, s: &G::State) -> f64 {
        if self.game.is_terminal(s) {
            return self.game.utility(s, self.player);
        }
        let key = self.game.infoset_offset(s);
        if self.game.player(s) == self.player {
            return self.after(s, self.policy[&key]);
        }
        self.opponent(key, self.game.num_actions(s))
            .into_iter()
            .enumerate()
            .filter(|&(_, p)| p > 0.0)
            .map(|(a, p)| p * self.after(s, a))
            .sum()
    }

    fn solve(&mut self) {
        let mut order: Vec<(usize, u64)> =
            self.infosets.iter().map(|(&k, i)| (i.depth, k)).collect();
        order.sort_unstable_by(|a, b| b.cmp(a));
        for (_, key) in order {
            let infoset = &self.infosets[&key];
            let n = self.game.num_actions(&infoset.histories[0].1);
            let mut best = (f64::NEG_INFINITY, 0);
            for a in 0..n {
                let v: f64 = infoset
                    .histories
                    .iter()
                    .map(|(w, h)| w * self.after(h, a))
                    .sum();
                if v > best.0 {
                    best = (v, a);
                }
            }
            self.policy.insert(key, best.1);
        }
    }
}

pub fn best_response_value<G: ExactGame>(game: &G, storage: &Storage, player: u8) -> f64 {
    let mut br = BestResponse {
        game,
        storage,
        player,
        infosets: HashMap::new(),
        policy: HashMap::new(),
    };
    let deals = game.deals();
    for (p, s) in &deals {
        br.collect(s, *p, 0);
    }
    br.solve();
    deals.iter().map(|(p, s)| p * br.value(s)).sum()
}

pub fn exploitability<G: ExactGame>(game: &G, storage: &Storage) -> f64 {
    (best_response_value(game, storage, 0) + best_response_value(game, storage, 1)) / 2.0
}
