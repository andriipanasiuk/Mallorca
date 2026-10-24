use crate::cards::Card;
use std::sync::OnceLock;

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub enum Category {
    HighCard,
    Pair,
    TwoPair,
    Trips,
    Straight,
    Flush,
    FullHouse,
    Quads,
    StraightFlush,
}

const CLASS_COUNTS: [u16; 9] = [1277, 2860, 858, 858, 10, 1277, 156, 156, 10];
const MAX_CARDS: usize = 7;
const MIN_CARDS: usize = 5;

struct Tables {
    flush: Vec<u16>,
    plain: Vec<u16>,
    skipped: [[[u32; 5]; MAX_CARDS + 1]; 13],
    offset: [u32; MAX_CARDS + 1],
}

fn tables() -> &'static Tables {
    static TABLES: OnceLock<Tables> = OnceLock::new();
    TABLES.get_or_init(build_tables)
}

fn pack(category: Category, tiebreak: [usize; 5]) -> u64 {
    let mut key = category as u64;
    for t in tiebreak {
        key = key << 4 | t as u64;
    }
    key
}

fn straight_top(mask: u16) -> Option<usize> {
    (4..13)
        .rev()
        .find(|&top| (mask >> (top - 4)) & 0b11111 == 0b11111)
        .or_else(|| (mask & 0b1000000001111 == 0b1000000001111).then_some(3))
}

fn top_ranks(counts: &[u8; 13], exclude: &[usize], min_count: u8) -> [usize; 5] {
    let mut out = [0; 5];
    let mut n = 0;
    for r in (0..13).rev() {
        if n < 5 && counts[r] >= min_count && !exclude.contains(&r) {
            out[n] = r;
            n += 1;
        }
    }
    out
}

fn flush_key(mask: u16) -> u64 {
    match straight_top(mask) {
        Some(top) => pack(Category::StraightFlush, [top, 0, 0, 0, 0]),
        None => {
            let mut counts = [0u8; 13];
            for (r, c) in counts.iter_mut().enumerate() {
                *c = (mask >> r & 1) as u8;
            }
            pack(Category::Flush, top_ranks(&counts, &[], 1))
        }
    }
}

fn plain_key(counts: &[u8; 13]) -> u64 {
    let mut mask = 0u16;
    for (r, &c) in counts.iter().enumerate() {
        if c > 0 {
            mask |= 1 << r;
        }
    }
    let quads = top_ranks(counts, &[], 4)[0];
    if counts[quads] == 4 {
        let kicker = top_ranks(counts, &[quads], 1)[0];
        return pack(Category::Quads, [quads, kicker, 0, 0, 0]);
    }
    let trips = top_ranks(counts, &[], 3)[0];
    let has_trips = counts[trips] >= 3;
    if has_trips {
        let pair = top_ranks(counts, &[trips], 2)[0];
        if counts[pair] >= 2 && pair != trips {
            return pack(Category::FullHouse, [trips, pair, 0, 0, 0]);
        }
    }
    if let Some(top) = straight_top(mask) {
        return pack(Category::Straight, [top, 0, 0, 0, 0]);
    }
    if has_trips {
        let kickers = top_ranks(counts, &[trips], 1);
        return pack(Category::Trips, [trips, kickers[0], kickers[1], 0, 0]);
    }
    let pairs = top_ranks(counts, &[], 2);
    if counts[pairs[0]] >= 2 {
        if counts[pairs[1]] >= 2 && pairs[1] != pairs[0] {
            let kicker = top_ranks(counts, &[pairs[0], pairs[1]], 1)[0];
            return pack(Category::TwoPair, [pairs[0], pairs[1], kicker, 0, 0]);
        }
        let kickers = top_ranks(counts, &[pairs[0]], 1);
        return pack(
            Category::Pair,
            [pairs[0], kickers[0], kickers[1], kickers[2], 0],
        );
    }
    pack(Category::HighCard, top_ranks(counts, &[], 1))
}

fn dense_index(t: &Tables, counts: &[u8; 13], total: usize) -> usize {
    let mut remaining = total;
    let mut index = t.offset[total];
    for (r, &c) in counts.iter().enumerate() {
        index += t.skipped[r][remaining][c as usize];
        remaining -= c as usize;
    }
    index as usize
}

fn build_tables() -> Tables {
    let mut ways = [[0u32; MAX_CARDS + 1]; 14];
    ways[13][0] = 1;
    for r in (0..13).rev() {
        for total in 0..=MAX_CARDS {
            ways[r][total] = (0..=total.min(4)).map(|c| ways[r + 1][total - c]).sum();
        }
    }
    let mut skipped = [[[0u32; 5]; MAX_CARDS + 1]; 13];
    for r in 0..13 {
        for remaining in 0..=MAX_CARDS {
            let mut acc = 0;
            for c in 0..5 {
                skipped[r][remaining][c] = acc;
                if c <= remaining {
                    acc += ways[r + 1][remaining - c];
                }
            }
        }
    }
    let mut offset = [0u32; MAX_CARDS + 1];
    for total in MIN_CARDS..MAX_CARDS {
        offset[total + 1] = offset[total] + ways[0][total];
    }
    let size = (offset[MAX_CARDS] + ways[0][MAX_CARDS]) as usize;
    let mut t = Tables {
        flush: vec![0; 1 << 13],
        plain: vec![0; size],
        skipped,
        offset,
    };

    let mut flush_keys = vec![0u64; 1 << 13];
    for mask in 0u16..(1 << 13) {
        if (MIN_CARDS..=MAX_CARDS).contains(&(mask.count_ones() as usize)) {
            flush_keys[mask as usize] = flush_key(mask);
        }
    }
    let mut plain_keys = vec![0u64; size];
    for total in MIN_CARDS..=MAX_CARDS {
        let mut counts = [0u8; 13];
        fill(&t, &mut counts, 0, total, total, &mut plain_keys);
    }

    let mut classes: Vec<u64> = flush_keys
        .iter()
        .chain(plain_keys.iter())
        .copied()
        .filter(|&k| k != 0)
        .collect();
    classes.sort_unstable();
    classes.dedup();
    debug_assert_eq!(classes.len(), 7462);
    debug_assert_eq!(
        CLASS_COUNTS.iter().map(|&c| c as usize).sum::<usize>(),
        classes.len()
    );
    let lookup = |key: u64| classes.binary_search(&key).unwrap() as u16;
    for (slot, &key) in t.flush.iter_mut().zip(flush_keys.iter()) {
        if key != 0 {
            *slot = lookup(key);
        }
    }
    for (slot, &key) in t.plain.iter_mut().zip(plain_keys.iter()) {
        if key != 0 {
            *slot = lookup(key);
        }
    }
    t
}

fn fill(
    t: &Tables,
    counts: &mut [u8; 13],
    r: usize,
    remaining: usize,
    total: usize,
    keys: &mut [u64],
) {
    if r == 13 {
        if remaining == 0 {
            keys[dense_index(t, counts, total)] = plain_key(counts);
        }
        return;
    }
    for c in 0..=remaining.min(4) {
        counts[r] = c as u8;
        fill(t, counts, r + 1, remaining - c, total, keys);
    }
    counts[r] = 0;
}

pub fn rank(cards: &[Card]) -> u16 {
    debug_assert!((MIN_CARDS..=MAX_CARDS).contains(&cards.len()));
    let t = tables();
    let mut counts = [0u8; 13];
    let mut suits = [0u16; 4];
    for &card in cards {
        let r = (card >> 2) as usize;
        counts[r] += 1;
        suits[(card & 3) as usize] |= 1 << r;
    }
    for mask in suits {
        if mask.count_ones() >= 5 {
            return t.flush[mask as usize];
        }
    }
    t.plain[dense_index(t, &counts, cards.len())]
}

pub fn category(rank: u16) -> Category {
    const ORDER: [Category; 9] = [
        Category::HighCard,
        Category::Pair,
        Category::TwoPair,
        Category::Trips,
        Category::Straight,
        Category::Flush,
        Category::FullHouse,
        Category::Quads,
        Category::StraightFlush,
    ];
    let mut upper = 0u16;
    for (cat, &n) in ORDER.iter().zip(CLASS_COUNTS.iter()) {
        upper += n;
        if rank < upper {
            return *cat;
        }
    }
    panic!("rank out of range: {rank}");
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cards::parse_cards;
    use crate::rng::Rng;

    fn r(s: &str) -> u16 {
        rank(&parse_cards(s).unwrap())
    }

    #[test]
    fn royal_flush_is_top_rank() {
        assert_eq!(r("As Ks Qs Js Ts"), 7461);
    }

    #[test]
    fn worst_hand_is_zero() {
        assert_eq!(r("7c 5d 4h 3s 2c"), 0);
    }

    #[test]
    fn wheel_is_below_six_high_straight() {
        assert!(r("5c 4d 3h 2s Ac") < r("6c 5d 4h 3s 2c"));
    }

    #[test]
    fn categories_of_known_hands() {
        assert_eq!(category(r("As Ks Qs Js Ts")), Category::StraightFlush);
        assert_eq!(category(r("7c 5d 4h 3s 2c")), Category::HighCard);
        assert_eq!(category(r("5c 4d 3h 2s Ac")), Category::Straight);
        assert_eq!(category(r("6c 5d 4h 3s 2c")), Category::Straight);
        assert_eq!(category(r("9c 9d 9h 9s 2c")), Category::Quads);
        assert_eq!(category(r("9c 9d 9h 2s 2c")), Category::FullHouse);
        assert_eq!(category(r("9c 7c 5c 3c 2c")), Category::Flush);
        assert_eq!(category(r("9c 9d 9h 5s 2c")), Category::Trips);
        assert_eq!(category(r("9c 9d 5h 5s 2c")), Category::TwoPair);
        assert_eq!(category(r("9c 9d 6h 5s 2c")), Category::Pair);
    }

    #[test]
    fn kickers_and_ties() {
        assert!(r("Ac Ad Kh 5s 2c") > r("Ac Ad Qh Js 9c"));
        assert_eq!(r("Ac Ad Kh 5s 2c"), r("As Ah Kd 5c 2d"));
        assert!(r("Ac Kc Qc Jc 9c") > r("Ac Kc Qc Jc 8c"));
    }

    fn best_of_subsets(cards: &[Card]) -> u16 {
        let n = cards.len();
        let mut best = 0;
        for mask in 0u32..(1 << n) {
            if mask.count_ones() != 5 {
                continue;
            }
            let hand: Vec<Card> = (0..n)
                .filter(|i| mask >> i & 1 == 1)
                .map(|i| cards[i])
                .collect();
            best = best.max(rank(&hand));
        }
        best
    }

    #[test]
    fn seven_and_six_card_hands_equal_best_five_card_subset() {
        let mut rng = Rng::new(99);
        let mut deck: Vec<Card> = (0..52).collect();
        for _ in 0..1000 {
            rng.shuffle(&mut deck);
            assert_eq!(rank(&deck[..7]), best_of_subsets(&deck[..7]));
            assert_eq!(rank(&deck[..6]), best_of_subsets(&deck[..6]));
        }
    }

    #[test]
    #[ignore]
    fn all_five_card_hands() {
        let mut seen = vec![false; 7462];
        let mut by_category = [0u32; 9];
        let mut total = 0u32;
        for a in 0..52u8 {
            for b in a + 1..52 {
                for c in b + 1..52 {
                    for d in c + 1..52 {
                        for e in d + 1..52 {
                            let v = rank(&[a, b, c, d, e]);
                            seen[v as usize] = true;
                            by_category[category(v) as usize] += 1;
                            total += 1;
                        }
                    }
                }
            }
        }
        assert_eq!(total, 2_598_960);
        assert_eq!(seen.iter().filter(|&&s| s).count(), 7462);
        assert_eq!(
            by_category,
            [1_302_540, 1_098_240, 123_552, 54_912, 10_200, 5_108, 3_744, 624, 40]
        );
    }
}
