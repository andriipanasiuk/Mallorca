const HANDS_PER_DEAL: f64 = 2.0;
const Z95: f64 = 1.96;

#[derive(Debug, Clone, Default)]
pub struct Stats {
    n: u64,
    mean: f64,
    m2: f64,
}

impl Stats {
    pub fn new() -> Stats {
        Stats::default()
    }

    pub fn push(&mut self, value: f64) {
        self.n += 1;
        let delta = value - self.mean;
        self.mean += delta / self.n as f64;
        self.m2 += delta * (value - self.mean);
    }

    pub fn count(&self) -> u64 {
        self.n
    }

    pub fn mean(&self) -> f64 {
        self.mean
    }

    pub fn std(&self) -> f64 {
        if self.n < 2 {
            0.0
        } else {
            (self.m2 / (self.n - 1) as f64).sqrt()
        }
    }

    pub fn half_width95(&self) -> f64 {
        if self.n < 2 {
            0.0
        } else {
            Z95 * self.std() / (self.n as f64).sqrt()
        }
    }
}

pub fn bb_per_100(chips_per_deal: f64, big_blind: u32) -> f64 {
    chips_per_deal / HANDS_PER_DEAL / f64::from(big_blind) * 100.0
}

#[cfg(test)]
mod tests {
    use super::*;

    fn filled(values: &[f64]) -> Stats {
        let mut stats = Stats::new();
        for &v in values {
            stats.push(v);
        }
        stats
    }

    #[test]
    fn known_sample_has_known_mean_and_interval() {
        let stats = filled(&[2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0]);
        assert_eq!(stats.count(), 8);
        assert!((stats.mean() - 5.0).abs() < 1e-12);
        let std = (32.0f64 / 7.0).sqrt();
        assert!((stats.std() - std).abs() < 1e-12);
        let half = 1.96 * std / 8.0f64.sqrt();
        assert!((stats.half_width95() - half).abs() < 1e-12);
    }

    #[test]
    fn short_samples_have_no_spread() {
        assert_eq!(Stats::new().std(), 0.0);
        assert_eq!(filled(&[3.0]).std(), 0.0);
        assert_eq!(filled(&[3.0]).half_width95(), 0.0);
    }

    #[test]
    fn identical_values_have_zero_interval() {
        let stats = filled(&[0.0; 100]);
        assert_eq!(stats.mean(), 0.0);
        assert_eq!(stats.half_width95(), 0.0);
    }

    #[test]
    fn chips_per_deal_convert_to_bb_per_100() {
        assert!((bb_per_100(40.0, 20) - 100.0).abs() < 1e-12);
        assert!((bb_per_100(-8.0, 20) - -20.0).abs() < 1e-12);
    }
}
