use std::fmt;
use std::sync::atomic::{AtomicU32, Ordering};

use crate::preset::CfrParams;
use crate::rng::Rng;

pub const MAX_ACTIONS: usize = 16;

pub trait Game: Sync {
    type State: Clone;
    fn root(&self, rng: &mut Rng) -> Self::State;
    fn is_terminal(&self, s: &Self::State) -> bool;
    fn utility(&self, s: &Self::State, player: u8) -> f64;
    fn player(&self, s: &Self::State) -> u8;
    fn num_actions(&self, s: &Self::State) -> usize;
    fn infoset_offset(&self, s: &Self::State) -> u64;
    fn apply(&self, s: &Self::State, action: usize, rng: &mut Rng) -> Self::State;
    fn infoset_len(&self) -> u64;
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum StorageError {
    Length { expected: usize, actual: usize },
}

impl fmt::Display for StorageError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            StorageError::Length { expected, actual } => {
                write!(f, "storage needs {expected} bytes, got {actual}")
            }
        }
    }
}

impl std::error::Error for StorageError {}

pub struct Storage {
    regrets: Vec<AtomicU32>,
    strategy: Vec<AtomicU32>,
}

fn zeros(len: u64) -> Vec<AtomicU32> {
    (0..len).map(|_| AtomicU32::new(0)).collect()
}

fn load(cell: &AtomicU32) -> f32 {
    f32::from_bits(cell.load(Ordering::Relaxed))
}

fn store(cell: &AtomicU32, v: f32) {
    cell.store(v.to_bits(), Ordering::Relaxed);
}

impl Storage {
    pub fn new(len: u64) -> Storage {
        Storage {
            regrets: zeros(len),
            strategy: zeros(len),
        }
    }

    pub fn len(&self) -> u64 {
        self.regrets.len() as u64
    }

    pub fn is_empty(&self) -> bool {
        self.regrets.is_empty()
    }

    pub fn regret(&self, i: u64) -> f32 {
        load(&self.regrets[i as usize])
    }

    pub fn add_regret(&self, i: u64, v: f32) {
        let cell = &self.regrets[i as usize];
        store(cell, load(cell) + v);
    }

    pub fn strategy_sum(&self, i: u64) -> f32 {
        load(&self.strategy[i as usize])
    }

    pub fn add_strategy(&self, i: u64, v: f32) {
        let cell = &self.strategy[i as usize];
        store(cell, load(cell) + v);
    }

    pub fn discount(&self, t: u64, params: &CfrParams) {
        let t = t as f64;
        let positive = t.powf(params.alpha) / (t.powf(params.alpha) + 1.0);
        let negative = t.powf(params.beta) / (t.powf(params.beta) + 1.0);
        let strategy = (t / (t + 1.0)).powf(params.gamma);
        for cell in &self.regrets {
            let r = load(cell);
            let factor = if r > 0.0 { positive } else { negative };
            store(cell, (f64::from(r) * factor) as f32);
        }
        for cell in &self.strategy {
            store(cell, (f64::from(load(cell)) * strategy) as f32);
        }
    }

    pub fn average_strategy(&self, offset: u64, n: usize) -> Vec<f32> {
        let sums: Vec<f32> = (0..n as u64)
            .map(|a| self.strategy_sum(offset + a))
            .collect();
        let total: f32 = sums.iter().sum();
        if total > 0.0 {
            sums.iter().map(|s| s / total).collect()
        } else {
            vec![1.0 / n as f32; n]
        }
    }

    pub fn to_bytes(&self) -> Vec<u8> {
        let mut out = Vec::with_capacity(self.regrets.len() * 8);
        for cell in self.regrets.iter().chain(&self.strategy) {
            out.extend_from_slice(&load(cell).to_le_bytes());
        }
        out
    }

    pub fn from_bytes(len: u64, bytes: &[u8]) -> Result<Storage, StorageError> {
        let expected = len as usize * 8;
        if bytes.len() != expected {
            return Err(StorageError::Length {
                expected,
                actual: bytes.len(),
            });
        }
        let mut regrets: Vec<AtomicU32> = bytes
            .as_chunks::<4>()
            .0
            .iter()
            .map(|&c| AtomicU32::new(u32::from_le_bytes(c)))
            .collect();
        let strategy = regrets.split_off(len as usize);
        Ok(Storage { regrets, strategy })
    }
}

pub fn regret_matching(regrets: &[f32], out: &mut [f32]) {
    let total: f32 = regrets.iter().map(|r| r.max(0.0)).sum();
    if total > 0.0 {
        for (o, r) in out.iter_mut().zip(regrets) {
            *o = r.max(0.0) / total;
        }
    } else {
        out.fill(1.0 / regrets.len() as f32);
    }
}

fn sample(strategy: &[f32], rng: &mut Rng) -> usize {
    let x = rng.next_f64() as f32;
    let mut acc = 0.0;
    for (a, &p) in strategy.iter().enumerate() {
        acc += p;
        if x < acc {
            return a;
        }
    }
    strategy
        .iter()
        .rposition(|&p| p > 0.0)
        .unwrap_or(strategy.len() - 1)
}

pub fn iterate<G: Game>(game: &G, storage: &Storage, traverser: u8, rng: &mut Rng) {
    let root = game.root(rng);
    traverse(game, storage, &root, traverser, rng);
}

fn traverse<G: Game>(
    game: &G,
    storage: &Storage,
    s: &G::State,
    traverser: u8,
    rng: &mut Rng,
) -> f64 {
    if game.is_terminal(s) {
        return game.utility(s, traverser);
    }
    let n = game.num_actions(s);
    assert!(n <= MAX_ACTIONS, "{n} actions exceed {MAX_ACTIONS}");
    let offset = game.infoset_offset(s);
    let mut regrets = [0.0f32; MAX_ACTIONS];
    for (a, r) in regrets[..n].iter_mut().enumerate() {
        *r = storage.regret(offset + a as u64);
    }
    let mut strategy = [0.0f32; MAX_ACTIONS];
    regret_matching(&regrets[..n], &mut strategy[..n]);
    if game.player(s) == traverser {
        let mut values = [0.0f64; MAX_ACTIONS];
        let mut ev = 0.0;
        for a in 0..n {
            let child = game.apply(s, a, rng);
            values[a] = traverse(game, storage, &child, traverser, rng);
            ev += f64::from(strategy[a]) * values[a];
        }
        for (a, v) in values[..n].iter().enumerate() {
            storage.add_regret(offset + a as u64, (v - ev) as f32);
        }
        ev
    } else {
        for (a, &p) in strategy[..n].iter().enumerate() {
            storage.add_strategy(offset + a as u64, p);
        }
        let a = sample(&strategy[..n], rng);
        let child = game.apply(s, a, rng);
        traverse(game, storage, &child, traverser, rng)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn params(alpha: f64, beta: f64, gamma: f64) -> CfrParams {
        CfrParams {
            alpha,
            beta,
            gamma,
            discount_interval: 1,
        }
    }

    #[test]
    fn regret_matching_normalizes_positive_regrets() {
        let mut out = [0.0f32; 3];
        regret_matching(&[1.0, -1.0, 3.0], &mut out);
        assert_eq!(out, [0.25, 0.0, 0.75]);
    }

    #[test]
    fn regret_matching_all_negative_is_uniform() {
        let mut out = [0.0f32; 4];
        regret_matching(&[-1.0, -2.0, 0.0, -0.5], &mut out);
        assert_eq!(out, [0.25; 4]);
    }

    #[test]
    fn discount_applies_dcfr_factors() {
        let s = Storage::new(3);
        s.add_regret(0, 8.0);
        s.add_regret(1, -6.0);
        s.add_strategy(2, 4.0);
        s.discount(1, &params(1.5, 0.0, 2.0));
        assert_eq!(s.regret(0), 4.0);
        assert_eq!(s.regret(1), -3.0);
        assert_eq!(s.strategy_sum(2), 1.0);
    }

    #[test]
    fn discount_at_later_t() {
        let s = Storage::new(2);
        s.add_regret(0, 9.0);
        s.add_strategy(1, 9.0);
        s.discount(2, &params(1.0, 0.0, 1.0));
        assert!((s.regret(0) - 6.0).abs() < 1e-6);
        assert!((s.strategy_sum(1) - 6.0).abs() < 1e-6);
    }

    #[test]
    fn average_strategy_normalizes_and_defaults_to_uniform() {
        let s = Storage::new(5);
        s.add_strategy(0, 1.0);
        s.add_strategy(1, 3.0);
        assert_eq!(s.average_strategy(0, 2), vec![0.25, 0.75]);
        assert_eq!(s.average_strategy(2, 3), vec![1.0 / 3.0; 3]);
    }

    #[test]
    fn bytes_round_trip() {
        let s = Storage::new(3);
        s.add_regret(0, 1.5);
        s.add_regret(2, -2.25);
        s.add_strategy(1, 7.0);
        let bytes = s.to_bytes();
        assert_eq!(bytes.len(), 24);
        assert_eq!(&bytes[0..4], &1.5f32.to_le_bytes());
        assert_eq!(&bytes[12..16], &0.0f32.to_le_bytes());
        assert_eq!(&bytes[16..20], &7.0f32.to_le_bytes());
        let r = Storage::from_bytes(3, &bytes).unwrap();
        assert_eq!(r.regret(0), 1.5);
        assert_eq!(r.regret(2), -2.25);
        assert_eq!(r.strategy_sum(1), 7.0);
    }

    #[test]
    fn from_bytes_rejects_wrong_length() {
        let bytes = Storage::new(3).to_bytes();
        assert_eq!(
            Storage::from_bytes(3, &bytes[..23]).err(),
            Some(StorageError::Length {
                expected: 24,
                actual: 23
            })
        );
        assert!(Storage::from_bytes(4, &bytes).is_err());
    }

    #[test]
    fn storage_is_sync() {
        fn assert_sync<T: Sync>() {}
        assert_sync::<Storage>();
    }
}
