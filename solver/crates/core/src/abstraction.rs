use crate::cards::Card;
use crate::eval::rank;
use crate::iso::{street_indexers, HandIndexer};
use crate::strategy::{StrategyError, StrategyFile};
use crate::tree::Street;
use std::sync::OnceLock;

const LIVE_HOLES: usize = 1081;
const LIVE_OPPONENTS: f32 = 990.0;

fn river_indexer() -> &'static HandIndexer {
    static INDEXER: OnceLock<HandIndexer> = OnceLock::new();
    INDEXER.get_or_init(|| HandIndexer::new(&[2, 5]))
}

fn below_and_equal(sorted: &[u16], r: u16) -> (usize, usize) {
    let below = sorted.partition_point(|&x| x < r);
    let at_most = sorted.partition_point(|&x| x <= r);
    (below, at_most - below)
}

pub fn river_strength(board: &[Card; 5]) -> Vec<(u64, f32)> {
    let live: Vec<Card> = (0..52u8).filter(|c| !board.contains(c)).collect();
    let mut seven = [0 as Card; 7];
    seven[2..].copy_from_slice(board);
    let mut holes: Vec<(usize, usize, u16)> = Vec::with_capacity(LIVE_HOLES);
    let mut all: Vec<u16> = Vec::with_capacity(LIVE_HOLES);
    let mut per_card: Vec<Vec<u16>> = vec![Vec::with_capacity(46); live.len()];
    for i in 0..live.len() {
        for j in (i + 1)..live.len() {
            seven[0] = live[i];
            seven[1] = live[j];
            let r = rank(&seven);
            holes.push((i, j, r));
            all.push(r);
            per_card[i].push(r);
            per_card[j].push(r);
        }
    }
    all.sort_unstable();
    for ranks in per_card.iter_mut() {
        ranks.sort_unstable();
    }
    let indexer = river_indexer();
    holes
        .iter()
        .map(|&(i, j, r)| {
            let (all_below, all_equal) = below_and_equal(&all, r);
            let (a_below, a_equal) = below_and_equal(&per_card[i], r);
            let (b_below, b_equal) = below_and_equal(&per_card[j], r);
            let wins = all_below - a_below - b_below;
            let ties = all_equal + 1 - a_equal - b_equal;
            seven[0] = live[i];
            seven[1] = live[j];
            let hs = (wins as f32 + 0.5 * ties as f32) / LIVE_OPPONENTS;
            (indexer.index(&seven), hs)
        })
        .collect()
}

pub fn percentile_buckets(values: &[f32], buckets: u16) -> Vec<u8> {
    assert!((1..=256).contains(&buckets));
    let mut sorted = values.to_vec();
    sorted.sort_unstable_by(f32::total_cmp);
    let n = sorted.len();
    let thresholds: Vec<f32> = (1..buckets as usize)
        .map(|b| sorted[(b * n / buckets as usize).min(n - 1)])
        .collect();
    drop(sorted);
    values
        .iter()
        .map(|&v| thresholds.partition_point(|&t| t <= v) as u8)
        .collect()
}

pub fn aggregate_hs2(indexers: &[HandIndexer; 4], street: Street, index: u64, next: &[f32]) -> f32 {
    let (round, target) = match street {
        Street::Turn => (2, 3),
        Street::Flop => (1, 2),
        _ => panic!("aggregate_hs2 needs flop or turn"),
    };
    let mut cards = indexers[round].unindex(1, index);
    let mut dead = 0u64;
    for &c in &cards {
        dead |= 1 << c;
    }
    cards.push(0);
    let last = cards.len() - 1;
    let mut sum = 0.0f32;
    let mut live = 0u32;
    for c in 0..52u8 {
        if dead & (1 << c) != 0 {
            continue;
        }
        cards[last] = c;
        let v = next[indexers[target].index(&cards) as usize];
        sum += v * v;
        live += 1;
    }
    sum / live as f32
}

pub struct Abstraction {
    indexers: [HandIndexer; 4],
    tables: [Vec<u8>; 3],
}

impl Abstraction {
    pub fn new(tables: [Vec<u8>; 3]) -> Abstraction {
        Abstraction {
            indexers: street_indexers(),
            tables,
        }
    }

    pub fn from_strategy(file: &StrategyFile) -> Result<Abstraction, StrategyError> {
        const POSTFLOP: [Street; 3] = [Street::Flop, Street::Turn, Street::River];
        let mut tables: [Vec<u8>; 3] = Default::default();
        let mut next = file.tables.iter();
        for (slot, street) in tables.iter_mut().zip(POSTFLOP) {
            if !file.includes(street) {
                return Err(StrategyError::MissingStreet(street));
            }
            *slot = next
                .next()
                .ok_or(StrategyError::MissingStreet(street))?
                .clone();
        }
        if next.next().is_some() {
            return Err(StrategyError::Inconsistent);
        }
        let abstraction = Abstraction::new(tables);
        let buckets = file.tree.buckets();
        for street in POSTFLOP {
            let table = abstraction.table_bytes(street);
            if table.len() as u64 != abstraction.class_count(street) {
                return Err(StrategyError::Inconsistent);
            }
            let limit = buckets[street.as_index()];
            if table.iter().any(|&b| u16::from(b) >= limit) {
                return Err(StrategyError::BucketOutOfRange(street));
            }
        }
        Ok(abstraction)
    }

    pub fn bucket(&self, street: Street, hole: [Card; 2], board: &[Card]) -> u16 {
        let s = street.as_index();
        let mut cards = [0 as Card; 7];
        cards[..2].copy_from_slice(&hole);
        cards[2..2 + board.len()].copy_from_slice(board);
        let index = self.indexers[s].index(&cards[..2 + board.len()]);
        if s == 0 {
            index as u16
        } else {
            self.tables[s - 1][index as usize] as u16
        }
    }

    pub fn class_count(&self, street: Street) -> u64 {
        self.indexers[street.as_index()].size(1)
    }

    pub fn table_bytes(&self, street: Street) -> &[u8] {
        &self.tables[street.as_index() - 1]
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cards::parse_cards;
    use crate::iso::board_indexer;
    use crate::rng::Rng;
    use std::collections::HashMap;

    fn river_indexer() -> HandIndexer {
        HandIndexer::new(&[2, 5])
    }

    fn deal(rng: &mut Rng, n: usize) -> Vec<Card> {
        let mut deck: Vec<Card> = (0..52).collect();
        rng.shuffle(&mut deck);
        deck.truncate(n);
        deck
    }

    fn brute_force(hole: [Card; 2], board: &[Card; 5]) -> f32 {
        let mut mine = hole.to_vec();
        mine.extend_from_slice(board);
        let my_rank = rank(&mine);
        let dead: Vec<Card> = mine.clone();
        let live: Vec<Card> = (0..52u8).filter(|c| !dead.contains(c)).collect();
        let mut score = 0.0f32;
        let mut total = 0.0f32;
        for i in 0..live.len() {
            for j in (i + 1)..live.len() {
                let mut theirs = vec![live[i], live[j]];
                theirs.extend_from_slice(board);
                let r = rank(&theirs);
                total += 1.0;
                if my_rank > r {
                    score += 1.0;
                } else if my_rank == r {
                    score += 0.5;
                }
            }
        }
        score / total
    }

    #[test]
    fn river_strength_matches_brute_force() {
        let indexer = river_indexer();
        let mut rng = Rng::new(41);
        for _ in 0..200 {
            let cards = deal(&mut rng, 7);
            let board: [Card; 5] = cards[2..7].try_into().unwrap();
            let strengths = river_strength(&board);
            assert_eq!(strengths.len(), 1081);
            let by_index: HashMap<u64, Vec<f32>> =
                strengths.iter().fold(HashMap::new(), |mut m, &(i, hs)| {
                    m.entry(i).or_default().push(hs);
                    m
                });
            let live: Vec<Card> = (0..52u8).filter(|c| !board.contains(c)).collect();
            for _ in 0..5 {
                let a = live[rng.below(47) as usize];
                let mut b = live[rng.below(47) as usize];
                while b == a {
                    b = live[rng.below(47) as usize];
                }
                let mut all = vec![a, b];
                all.extend_from_slice(&board);
                let idx = indexer.index(&all);
                let expected = brute_force([a, b], &board);
                for &hs in &by_index[&idx] {
                    assert!((hs - expected).abs() < 1e-6, "{hs} vs {expected}");
                }
            }
        }
    }

    #[test]
    fn river_strength_orders_aces_above_trash() {
        let indexer = river_indexer();
        let board: [Card; 5] = parse_cards("2c 7d 9h Js Kc").unwrap().try_into().unwrap();
        let strengths: HashMap<u64, f32> = river_strength(&board).into_iter().collect();
        let mut aces = parse_cards("As Ad").unwrap();
        aces.extend_from_slice(&board);
        let mut trash = parse_cards("3c 4d").unwrap();
        trash.extend_from_slice(&board);
        let a = strengths[&indexer.index(&aces)];
        let t = strengths[&indexer.index(&trash)];
        assert!(a > t, "{a} vs {t}");
    }

    #[test]
    fn percentile_buckets_split_evenly() {
        let values: Vec<f32> = (0..1000).map(|v| v as f32).collect();
        let buckets = percentile_buckets(&values, 8);
        let mut counts = [0usize; 8];
        for &b in &buckets {
            counts[b as usize] += 1;
        }
        assert_eq!(counts, [125; 8]);
        assert!(buckets.windows(2).all(|w| w[0] <= w[1]));
    }

    #[test]
    fn percentile_buckets_never_split_ties() {
        let mut rng = Rng::new(5);
        let values: Vec<f32> = (0..5000).map(|_| rng.below(37) as f32).collect();
        let buckets = percentile_buckets(&values, 8);
        let mut seen: HashMap<u32, u8> = HashMap::new();
        for (&v, &b) in values.iter().zip(&buckets) {
            assert_eq!(*seen.entry(v as u32).or_insert(b), b);
        }
        for (&v, &b) in values.iter().zip(&buckets) {
            for (&w, &c) in values.iter().zip(&buckets).take(200) {
                if v < w {
                    assert!(b <= c);
                }
            }
        }
    }

    #[test]
    fn percentile_buckets_cover_the_range() {
        let values: Vec<f32> = (0..1000).map(|v| (v * 7 % 1000) as f32).collect();
        let buckets = percentile_buckets(&values, 128);
        assert_eq!(*buckets.iter().max().unwrap(), 127);
        assert_eq!(*buckets.iter().min().unwrap(), 0);
    }

    #[test]
    fn bucket_is_suit_isomorphic() {
        let indexers = street_indexers();
        let sizes: Vec<usize> = (1..4).map(|s| indexers[s].size(1) as usize).collect();
        let tables = [
            (0..sizes[0]).map(|i| (i % 251) as u8).collect(),
            (0..sizes[1]).map(|i| (i % 241) as u8).collect(),
            (0..sizes[2]).map(|i| (i % 239) as u8).collect(),
        ];
        let abstraction = Abstraction::new(tables);
        let mut rng = Rng::new(77);
        for _ in 0..2000 {
            let cards = deal(&mut rng, 7);
            let mut perm = [0u8, 1, 2, 3];
            rng.shuffle(&mut perm);
            let mapped: Vec<Card> = cards
                .iter()
                .map(|&c| (c / 4) * 4 + perm[(c % 4) as usize])
                .collect();
            for (street, n) in [
                (Street::Preflop, 2usize),
                (Street::Flop, 5),
                (Street::Turn, 6),
                (Street::River, 7),
            ] {
                let a = abstraction.bucket(street, [cards[0], cards[1]], &cards[2..n]);
                let b = abstraction.bucket(street, [mapped[0], mapped[1]], &mapped[2..n]);
                assert_eq!(a, b);
            }
        }
    }

    #[test]
    fn class_counts_match_table_sizes() {
        let abstraction = Abstraction::new([vec![], vec![], vec![]]);
        assert_eq!(abstraction.class_count(Street::Flop), 1_286_792);
        assert_eq!(abstraction.class_count(Street::Turn), 13_960_050);
        assert_eq!(abstraction.class_count(Street::River), 123_156_254);
    }

    #[test]
    fn preflop_bucket_is_the_class_index() {
        let abstraction = Abstraction::new([vec![], vec![], vec![]]);
        let pair = abstraction.bucket(Street::Preflop, [0, 1], &[]);
        let other = abstraction.bucket(Street::Preflop, [0, 5], &[]);
        assert!(pair < 169 && other < 169);
        assert_ne!(pair, other);
    }

    #[test]
    fn aggregate_hs2_averages_squares_over_live_rivers() {
        let indexers = street_indexers();
        let mut next = vec![0.0f32; indexers[3].size(1) as usize];
        let turn_index = 4_000_000u64;
        let cards = indexers[2].unindex(1, turn_index);
        let mut expected = 0.0f32;
        let mut live = 0;
        for c in 0..52u8 {
            if cards.contains(&c) {
                continue;
            }
            let mut seven = cards.clone();
            seven.push(c);
            let idx = indexers[3].index(&seven) as usize;
            next[idx] = 0.1 + 0.01 * live as f32;
            live += 1;
        }
        assert_eq!(live, 46);
        for c in 0..52u8 {
            if cards.contains(&c) {
                continue;
            }
            let mut seven = cards.clone();
            seven.push(c);
            let v = next[indexers[3].index(&seven) as usize];
            expected += v * v;
        }
        expected /= 46.0;
        let got = aggregate_hs2(&indexers, Street::Turn, turn_index, &next);
        assert!((got - expected).abs() < 1e-6, "{got} vs {expected}");
    }

    #[test]
    #[ignore]
    fn river_buckets_are_monotone_in_strength_on_tiny() {
        let boards = board_indexer(5);
        let river = river_indexer();
        let mut hs = vec![f32::NAN; river.size(1) as usize];
        for b in 0..boards.size(0) {
            let cards = boards.unindex(0, b);
            let board: [Card; 5] = cards.try_into().unwrap();
            for (i, v) in river_strength(&board) {
                hs[i as usize] = v;
            }
        }
        assert!(hs.iter().all(|v| !v.is_nan()));
        let buckets = percentile_buckets(&hs, 8);
        let abstraction = Abstraction::new([vec![], vec![], buckets]);
        let mut rng = Rng::new(99);
        for _ in 0..50_000 {
            let i = rng.next_u64() % river.size(1);
            let j = rng.next_u64() % river.size(1);
            let (ci, cj) = (river.unindex(1, i), river.unindex(1, j));
            let bi = abstraction.bucket(Street::River, [ci[0], ci[1]], &ci[2..]);
            let bj = abstraction.bucket(Street::River, [cj[0], cj[1]], &cj[2..]);
            if hs[i as usize] > hs[j as usize] {
                assert!(bi >= bj);
            } else if hs[i as usize] < hs[j as usize] {
                assert!(bi <= bj);
            }
        }
    }
}
