use crate::cfr::Game;
use crate::games::exact::ExactGame;
use crate::rng::Rng;

pub struct KuhnPoker;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct KuhnState {
    cards: [u8; 2],
    history: u8,
}

const DECISIONS: u8 = 4;
const NEXT: [[u8; 2]; 4] = [[1, 2], [4, 3], [5, 6], [7, 8]];
const ACTOR: [u8; 4] = [0, 1, 1, 0];

pub const PASS: usize = 0;
pub const BET: usize = 1;

impl KuhnPoker {
    pub fn deal(&self, cards: [u8; 2]) -> KuhnState {
        KuhnState { cards, history: 0 }
    }

    pub fn offset(&self, history: &[usize], card: u8) -> u64 {
        let mut s = self.deal([card, card]);
        for &a in history {
            s.history = NEXT[s.history as usize][a];
        }
        self.infoset_offset(&s)
    }
}

impl Game for KuhnPoker {
    type State = KuhnState;

    fn root(&self, rng: &mut Rng) -> KuhnState {
        let first = rng.below(3) as u8;
        let second = (first + 1 + rng.below(2) as u8) % 3;
        self.deal([first, second])
    }

    fn is_terminal(&self, s: &KuhnState) -> bool {
        s.history >= DECISIONS
    }

    fn utility(&self, s: &KuhnState, player: u8) -> f64 {
        let showdown = if s.cards[0] > s.cards[1] { 1.0 } else { -1.0 };
        let first = match s.history {
            4 => showdown,
            5 => 1.0,
            6 | 8 => 2.0 * showdown,
            7 => -1.0,
            _ => panic!("history {} is not terminal", s.history),
        };
        if player == 0 {
            first
        } else {
            -first
        }
    }

    fn player(&self, s: &KuhnState) -> u8 {
        ACTOR[s.history as usize]
    }

    fn num_actions(&self, _s: &KuhnState) -> usize {
        2
    }

    fn infoset_offset(&self, s: &KuhnState) -> u64 {
        let card = s.cards[self.player(s) as usize];
        (u64::from(s.history) * 3 + u64::from(card)) * 2
    }

    fn apply(&self, s: &KuhnState, action: usize, _rng: &mut Rng) -> KuhnState {
        KuhnState {
            cards: s.cards,
            history: NEXT[s.history as usize][action],
        }
    }

    fn infoset_len(&self) -> u64 {
        u64::from(DECISIONS) * 3 * 2
    }
}

impl ExactGame for KuhnPoker {
    fn deals(&self) -> Vec<(f64, KuhnState)> {
        let mut out = Vec::new();
        for a in 0..3 {
            for b in 0..3 {
                if a != b {
                    out.push((1.0 / 6.0, self.deal([a, b])));
                }
            }
        }
        out
    }

    fn outcomes(&self, s: &KuhnState, action: usize) -> Vec<(f64, KuhnState)> {
        vec![(1.0, self.apply(s, action, &mut Rng::new(0)))]
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cfr::Storage;
    use crate::games::{exploitability, train};
    use crate::preset::CfrParams;

    const J: u8 = 0;
    const Q: u8 = 1;
    const K: u8 = 2;

    fn set(game: &KuhnPoker, s: &Storage, history: &[usize], card: u8, bet: f64) {
        let o = game.offset(history, card);
        s.add_strategy(o + PASS as u64, (1.0 - bet) as f32);
        s.add_strategy(o + BET as u64, bet as f32);
    }

    fn profile(
        game: &KuhnPoker,
        p0_bet: [f64; 3],
        p0_call: [f64; 3],
        p1: [[f64; 2]; 3],
    ) -> Storage {
        let s = Storage::new(game.infoset_len());
        for c in [J, Q, K] {
            set(game, &s, &[], c, p0_bet[c as usize]);
            set(game, &s, &[PASS, BET], c, p0_call[c as usize]);
            set(game, &s, &[PASS], c, p1[c as usize][0]);
            set(game, &s, &[BET], c, p1[c as usize][1]);
        }
        s
    }

    #[test]
    fn utilities_follow_the_rules() {
        let g = KuhnPoker;
        let mut rng = Rng::new(0);
        let play = |cards, actions: &[usize]| {
            let mut s = g.deal(cards);
            for &a in actions {
                s = g.apply(&s, a, &mut Rng::new(0));
            }
            assert!(g.is_terminal(&s));
            (g.utility(&s, 0), g.utility(&s, 1))
        };
        assert_eq!(play([K, J], &[PASS, PASS]), (1.0, -1.0));
        assert_eq!(play([J, K], &[BET, PASS]), (1.0, -1.0));
        assert_eq!(play([J, K], &[BET, BET]), (-2.0, 2.0));
        assert_eq!(play([K, Q], &[PASS, BET, PASS]), (-1.0, 1.0));
        assert_eq!(play([K, Q], &[PASS, BET, BET]), (2.0, -2.0));
        let root = g.root(&mut rng);
        assert_ne!(root.cards[0], root.cards[1]);
        assert!(root.cards.iter().all(|&c| c < 3));
    }

    #[test]
    fn offsets_are_distinct_and_in_range() {
        let g = KuhnPoker;
        let mut seen = std::collections::HashSet::new();
        for h in [&[][..], &[PASS], &[BET], &[PASS, BET]] {
            for c in [J, Q, K] {
                let o = g.offset(h, c);
                assert!(o + 2 <= g.infoset_len());
                assert!(seen.insert(o));
            }
        }
    }

    #[test]
    fn uniform_profile_exploitability() {
        let g = KuhnPoker;
        let s = Storage::new(g.infoset_len());
        assert!((exploitability(&g, &s) - 11.0 / 24.0).abs() < 1e-9);
    }

    #[test]
    fn nash_profile_has_zero_exploitability() {
        let g = KuhnPoker;
        let s = profile(
            &g,
            [0.0, 0.0, 0.0],
            [0.0, 1.0 / 3.0, 1.0],
            [[1.0 / 3.0, 0.0], [0.0, 1.0 / 3.0], [1.0, 1.0]],
        );
        assert!(exploitability(&g, &s).abs() < 1e-7);
    }

    #[test]
    fn always_bet_is_exploitable() {
        let g = KuhnPoker;
        let s = profile(&g, [1.0; 3], [1.0; 3], [[1.0; 2]; 3]);
        assert!(exploitability(&g, &s) > 0.1);
    }

    fn trained(iterations: u64) -> Storage {
        let params = CfrParams {
            alpha: 1.5,
            beta: 0.0,
            gamma: 2.0,
            discount_interval: 100,
        };
        train(&KuhnPoker, iterations, &params, 1)
    }

    #[test]
    fn dcfr_approaches_equilibrium_on_kuhn() {
        let e = exploitability(&KuhnPoker, &trained(100_000));
        assert!(e < 0.01, "exploitability {e}");
    }

    #[test]
    #[ignore]
    fn dcfr_converges_on_kuhn() {
        let e = exploitability(&KuhnPoker, &trained(12_800_000));
        assert!(e < 0.001, "exploitability {e}");
    }

    #[test]
    fn second_player_folds_the_jack_to_a_bet() {
        let fold = trained(100_000).average_strategy(KuhnPoker.offset(&[BET], J), 2)[PASS];
        assert!(fold > 0.99, "fold {fold}");
    }
}
