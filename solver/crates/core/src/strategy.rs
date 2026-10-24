use crate::iso::street_indexers;
use crate::tree::{Node, NodeId, Street, Tree};
use std::fmt;

pub const MAGIC: &[u8; 4] = b"MLCS";
pub const FORMAT_VERSION: u16 = 1;
pub const PLAY_FORMAT_VERSION: u16 = 2;
const KIND_TABLE: u8 = 0;
const KIND_THRESHOLDS: u8 = 1;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum StrategyError {
    BadMagic,
    UnknownVersion(u16),
    Truncated,
    Inconsistent,
    MissingStreet(Street),
    BucketOutOfRange(Street),
}

impl fmt::Display for StrategyError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            StrategyError::BadMagic => write!(f, "not a strategy file (bad magic)"),
            StrategyError::UnknownVersion(v) => write!(f, "unknown strategy format version {v}"),
            StrategyError::Truncated => write!(f, "strategy file is truncated"),
            StrategyError::Inconsistent => write!(f, "strategy file is inconsistent"),
            StrategyError::MissingStreet(street) => {
                write!(f, "strategy file has no table for the {street:?}")
            }
            StrategyError::BucketOutOfRange(street) => {
                write!(
                    f,
                    "strategy file has a {street:?} bucket beyond the tree's count"
                )
            }
        }
    }
}

impl std::error::Error for StrategyError {}

pub fn quantize(probabilities: &[f32]) -> Vec<u8> {
    let n = probabilities.len();
    let total: f64 = probabilities.iter().map(|&p| f64::from(p.max(0.0))).sum();
    let scaled: Vec<f64> = if total > 0.0 {
        probabilities
            .iter()
            .map(|&p| f64::from(p.max(0.0)) / total * 255.0)
            .collect()
    } else {
        vec![255.0 / n as f64; n]
    };
    let mut out: Vec<u32> = scaled.iter().map(|&x| x.floor() as u32).collect();
    let mut order: Vec<usize> = (0..n).collect();
    order.sort_by(|&a, &b| {
        let fa = scaled[a] - scaled[a].floor();
        let fb = scaled[b] - scaled[b].floor();
        fb.total_cmp(&fa).then(a.cmp(&b))
    });
    let mut sum: u32 = out.iter().sum();
    let mut turn = 0;
    while sum < 255 {
        out[order[turn % n]] += 1;
        sum += 1;
        turn += 1;
    }
    while sum > 255 {
        let largest = (0..n).max_by_key(|&i| out[i]).expect("not empty");
        out[largest] -= 1;
        sum -= 1;
    }
    out.into_iter().map(|x| x as u8).collect()
}

#[derive(Debug, Clone, PartialEq)]
pub struct StrategyFile {
    pub fingerprint: [u8; 32],
    pub small_blind: u32,
    pub big_blind: u32,
    pub stack: u32,
    pub streets: u8,
    pub iterations: u64,
    pub checkpoint: u32,
    pub tree: Tree,
    pub tables: Vec<Vec<u8>>,
    pub thresholds: Vec<(Street, Vec<f32>)>,
    pub probabilities: Vec<u8>,
    offsets: Vec<u64>,
}

const POSTFLOP: [Street; 3] = [Street::Flop, Street::Turn, Street::River];
const KNOWN_STREETS: u8 = 0b1111;

fn included(streets: u8, street: Street) -> bool {
    streets & (1 << street.as_index()) != 0
}

fn offsets_for(tree: &Tree, streets: u8) -> (Vec<u64>, u64) {
    let buckets = tree.buckets();
    let mut offsets = Vec::with_capacity(tree.nodes().len());
    let mut next = 0u64;
    for node in tree.nodes() {
        offsets.push(next);
        if let Node::Decision {
            street, actions, ..
        } = node
        {
            if included(streets, *street) {
                next += u64::from(buckets[street.as_index()]) * actions.len() as u64;
            }
        }
    }
    (offsets, next)
}

struct Cursor<'a> {
    bytes: &'a [u8],
    pos: usize,
}

impl<'a> Cursor<'a> {
    fn take(&mut self, n: usize) -> Result<&'a [u8], StrategyError> {
        let end = self.pos.checked_add(n).ok_or(StrategyError::Truncated)?;
        let slice = self
            .bytes
            .get(self.pos..end)
            .ok_or(StrategyError::Truncated)?;
        self.pos = end;
        Ok(slice)
    }

    fn u8(&mut self) -> Result<u8, StrategyError> {
        Ok(self.take(1)?[0])
    }

    fn u16(&mut self) -> Result<u16, StrategyError> {
        Ok(u16::from_le_bytes(
            self.take(2)?.try_into().expect("2 bytes"),
        ))
    }

    fn u32(&mut self) -> Result<u32, StrategyError> {
        Ok(u32::from_le_bytes(
            self.take(4)?.try_into().expect("4 bytes"),
        ))
    }

    fn u64(&mut self) -> Result<u64, StrategyError> {
        Ok(u64::from_le_bytes(
            self.take(8)?.try_into().expect("8 bytes"),
        ))
    }

    fn prefixed(&mut self) -> Result<&'a [u8], StrategyError> {
        let n = self.u32()? as usize;
        self.take(n)
    }
}

type Buckets = (Vec<Vec<u8>>, Vec<(Street, Vec<f32>)>);

fn read_table(t: &mut Cursor<'_>, street: Street) -> Result<Vec<u8>, StrategyError> {
    let table = t.prefixed().map_err(|_| StrategyError::Inconsistent)?;
    if table.len() as u64 != street_indexers()[street.as_index()].size(1) {
        return Err(StrategyError::Inconsistent);
    }
    Ok(table.to_vec())
}

fn read_thresholds(t: &mut Cursor<'_>, expected: u16) -> Result<Vec<f32>, StrategyError> {
    let count = t.u16().map_err(|_| StrategyError::Inconsistent)?;
    if count != expected.saturating_sub(1) {
        return Err(StrategyError::Inconsistent);
    }
    let bytes = t
        .take(usize::from(count) * 4)
        .map_err(|_| StrategyError::Inconsistent)?;
    let values: Vec<f32> = bytes
        .as_chunks::<4>()
        .0
        .iter()
        .map(|&c| f32::from_le_bytes(c))
        .collect();
    let ordered = values.windows(2).all(|w| w[0] <= w[1]);
    if !ordered || values.iter().any(|v| !v.is_finite()) {
        return Err(StrategyError::Inconsistent);
    }
    Ok(values)
}

fn read_buckets(
    section: &[u8],
    tree: &Tree,
    streets: u8,
    tagged: bool,
) -> Result<Buckets, StrategyError> {
    let mut tables = Vec::new();
    let mut thresholds = Vec::new();
    let mut t = Cursor {
        bytes: section,
        pos: 0,
    };
    for street in POSTFLOP.into_iter().filter(|&s| included(streets, s)) {
        let kind = if tagged {
            t.u8().map_err(|_| StrategyError::Inconsistent)?
        } else {
            KIND_TABLE
        };
        match kind {
            KIND_TABLE => tables.push(read_table(&mut t, street)?),
            KIND_THRESHOLDS => {
                let expected = tree.buckets()[street.as_index()];
                thresholds.push((street, read_thresholds(&mut t, expected)?));
            }
            _ => return Err(StrategyError::Inconsistent),
        }
    }
    if t.pos != section.len() {
        return Err(StrategyError::Inconsistent);
    }
    Ok((tables, thresholds))
}

impl StrategyFile {
    pub fn new(
        header: StrategyHeader,
        tree: Tree,
        tables: Vec<Vec<u8>>,
        probabilities: Vec<u8>,
    ) -> StrategyFile {
        let (offsets, _) = offsets_for(&tree, header.streets);
        StrategyFile {
            fingerprint: header.fingerprint,
            small_blind: header.small_blind,
            big_blind: header.big_blind,
            stack: header.stack,
            streets: header.streets,
            iterations: header.iterations,
            checkpoint: header.checkpoint,
            tree,
            tables,
            thresholds: Vec::new(),
            probabilities,
            offsets,
        }
    }

    pub fn with_thresholds(mut self, thresholds: Vec<(Street, Vec<f32>)>) -> StrategyFile {
        self.thresholds = thresholds;
        self
    }

    pub fn thresholds_of(&self, street: Street) -> Option<&[f32]> {
        self.thresholds
            .iter()
            .find(|(s, _)| *s == street)
            .map(|(_, t)| &t[..])
    }

    fn bucket_section(&self) -> Vec<u8> {
        let mut out = Vec::new();
        if self.thresholds.is_empty() {
            for table in &self.tables {
                out.extend_from_slice(&(table.len() as u32).to_le_bytes());
                out.extend_from_slice(table);
            }
            return out;
        }
        let mut tables = self.tables.iter();
        for street in POSTFLOP.into_iter().filter(|&s| self.includes(s)) {
            if let Some(t) = self.thresholds_of(street) {
                out.push(KIND_THRESHOLDS);
                out.extend_from_slice(&(t.len() as u16).to_le_bytes());
                for v in t {
                    out.extend_from_slice(&v.to_le_bytes());
                }
            } else if let Some(table) = tables.next() {
                out.push(KIND_TABLE);
                out.extend_from_slice(&(table.len() as u32).to_le_bytes());
                out.extend_from_slice(table);
            }
        }
        out
    }

    pub fn includes(&self, street: Street) -> bool {
        included(self.streets, street)
    }

    pub fn to_bytes(&self) -> Vec<u8> {
        let tree = self.tree.serialize();
        let mut out = Vec::with_capacity(
            128 + tree.len()
                + self.tables.iter().map(Vec::len).sum::<usize>()
                + self.probabilities.len(),
        );
        let version = if self.thresholds.is_empty() {
            FORMAT_VERSION
        } else {
            PLAY_FORMAT_VERSION
        };
        out.extend_from_slice(MAGIC);
        out.extend_from_slice(&version.to_le_bytes());
        out.extend_from_slice(&self.fingerprint);
        out.extend_from_slice(&self.small_blind.to_le_bytes());
        out.extend_from_slice(&self.big_blind.to_le_bytes());
        out.extend_from_slice(&self.stack.to_le_bytes());
        out.push(self.streets);
        out.extend_from_slice(&self.iterations.to_le_bytes());
        out.extend_from_slice(&self.checkpoint.to_le_bytes());
        out.extend_from_slice(&(tree.len() as u32).to_le_bytes());
        out.extend_from_slice(&tree);
        let section = self.bucket_section();
        out.extend_from_slice(&(section.len() as u32).to_le_bytes());
        out.extend_from_slice(&section);
        out.extend_from_slice(&self.probabilities);
        out
    }

    pub fn from_bytes(bytes: &[u8]) -> Result<StrategyFile, StrategyError> {
        let mut c = Cursor { bytes, pos: 0 };
        if c.take(4)? != MAGIC {
            return Err(StrategyError::BadMagic);
        }
        let version = c.u16()?;
        if version != FORMAT_VERSION && version != PLAY_FORMAT_VERSION {
            return Err(StrategyError::UnknownVersion(version));
        }
        let fingerprint: [u8; 32] = c.take(32)?.try_into().expect("32 bytes");
        let small_blind = c.u32()?;
        let big_blind = c.u32()?;
        let stack = c.u32()?;
        let streets = c.u8()?;
        let iterations = c.u64()?;
        let checkpoint = c.u32()?;
        let tree_bytes = c.prefixed()?;
        let table_section = c.prefixed()?;
        let probabilities = &bytes[c.pos..];
        if streets & 1 == 0 || streets & !KNOWN_STREETS != 0 {
            return Err(StrategyError::Inconsistent);
        }
        let tree = Tree::deserialize(tree_bytes).map_err(|_| StrategyError::Inconsistent)?;
        let (tables, thresholds) = read_buckets(
            table_section,
            &tree,
            streets,
            version == PLAY_FORMAT_VERSION,
        )?;
        let (offsets, expected) = offsets_for(&tree, streets);
        if probabilities.len() as u64 != expected {
            return Err(StrategyError::Inconsistent);
        }
        Ok(StrategyFile {
            fingerprint,
            small_blind,
            big_blind,
            stack,
            streets,
            iterations,
            checkpoint,
            tree,
            tables,
            thresholds,
            probabilities: probabilities.to_vec(),
            offsets,
        })
    }

    pub fn probabilities(&self, node: NodeId, bucket: u16) -> &[u8] {
        match self.tree.node(node) {
            Node::Decision {
                street, actions, ..
            } => {
                assert!(
                    self.includes(*street),
                    "street {street:?} is not in this file"
                );
                assert!(
                    bucket < self.tree.buckets()[street.as_index()],
                    "bucket {bucket} out of range for {street:?}"
                );
                let n = actions.len();
                let start = self.offsets[node as usize] as usize + usize::from(bucket) * n;
                &self.probabilities[start..start + n]
            }
            other => panic!("node {node} is not a decision: {other:?}"),
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct StrategyHeader {
    pub fingerprint: [u8; 32],
    pub small_blind: u32,
    pub big_blind: u32,
    pub stack: u32,
    pub streets: u8,
    pub iterations: u64,
    pub checkpoint: u32,
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::preset::Preset;
    use crate::rng::Rng;

    const TINY: &str = include_str!("../../../presets/tiny.toml");
    const FLOP_CLASSES: usize = 1_286_792;

    fn sample(streets: u8) -> StrategyFile {
        let preset = Preset::from_toml(TINY).unwrap();
        let tree = Tree::build(&preset);
        let mut tables = Vec::new();
        if streets & 2 != 0 {
            tables.push((0..FLOP_CLASSES).map(|i| (i % 8) as u8).collect());
        }
        let mut probabilities = Vec::new();
        let mut next = 0u8;
        for node in tree.nodes() {
            if let Node::Decision {
                street, actions, ..
            } = node
            {
                if streets & (1 << street.as_index()) == 0 {
                    continue;
                }
                let buckets = tree.buckets()[street.as_index()];
                for _ in 0..buckets {
                    let mut p = vec![0.0f32; actions.len()];
                    p[next as usize % actions.len()] = 1.0;
                    next = next.wrapping_add(1);
                    probabilities.extend(quantize(&p));
                }
            }
        }
        let header = StrategyHeader {
            fingerprint: [7; 32],
            small_blind: preset.small_blind,
            big_blind: preset.big_blind,
            stack: preset.stack,
            streets,
            iterations: 12_345,
            checkpoint: 3,
        };
        StrategyFile::new(header, tree, tables, probabilities)
    }

    #[test]
    fn quantize_thirds() {
        assert_eq!(quantize(&[1.0 / 3.0; 3]), vec![85, 85, 85]);
    }

    #[test]
    fn quantize_skewed_sums_to_255() {
        let q = quantize(&[0.999, 0.0005, 0.0005]);
        assert_eq!(q.iter().map(|&x| u32::from(x)).sum::<u32>(), 255);
        assert_eq!(q, vec![255, 0, 0]);
    }

    #[test]
    fn quantize_zeros_is_uniform() {
        assert_eq!(quantize(&[0.0, 0.0, 0.0]), vec![85, 85, 85]);
    }

    #[test]
    fn quantize_random_distributions() {
        let mut rng = Rng::new(5);
        for _ in 0..10_000 {
            let n = 2 + rng.below(6) as usize;
            let raw: Vec<f64> = (0..n)
                .map(|_| {
                    let x = rng.next_f64();
                    x * x * x
                })
                .collect();
            let total: f64 = raw.iter().sum();
            let p: Vec<f32> = raw.iter().map(|x| (x / total) as f32).collect();
            let q = quantize(&p);
            assert_eq!(q.iter().map(|&x| u32::from(x)).sum::<u32>(), 255, "{p:?}");
            for (a, b) in p.iter().zip(&q) {
                assert!(
                    (f64::from(*a) * 255.0 - f64::from(*b)).abs() <= 1.0,
                    "{p:?}"
                );
            }
        }
    }

    #[test]
    fn round_trip() {
        for streets in [0b0001, 0b0011] {
            let f = sample(streets);
            let g = StrategyFile::from_bytes(&f.to_bytes()).unwrap();
            assert_eq!(g, f);
        }
    }

    #[test]
    fn header_layout() {
        let f = sample(0b0011);
        let b = f.to_bytes();
        assert_eq!(&b[..4], b"MLCS");
        assert_eq!(&b[4..6], &1u16.to_le_bytes());
        assert_eq!(&b[6..38], &[7u8; 32]);
        assert_eq!(&b[38..42], &10u32.to_le_bytes());
        assert_eq!(&b[42..46], &20u32.to_le_bytes());
        assert_eq!(&b[46..50], &400u32.to_le_bytes());
        assert_eq!(b[50], 0b0011);
        assert_eq!(&b[51..59], &12_345u64.to_le_bytes());
        assert_eq!(&b[59..63], &3u32.to_le_bytes());
        assert!(b.ends_with(&f.probabilities));
    }

    #[test]
    fn bad_magic() {
        let mut b = sample(0b0011).to_bytes();
        b[0] = b'X';
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::BadMagic)
        );
    }

    #[test]
    fn version_three() {
        let mut b = sample(0b0011).to_bytes();
        b[4] = 3;
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::UnknownVersion(3))
        );
    }

    fn thresholds() -> Vec<f32> {
        (1..8).map(|i| i as f32 / 8.0).collect()
    }

    fn play_sample() -> StrategyFile {
        sample(0b1111).with_thresholds(vec![
            (Street::Turn, thresholds()),
            (Street::River, thresholds()),
        ])
    }

    #[test]
    fn play_file_round_trips_as_version_two() {
        let f = play_sample();
        let b = f.to_bytes();
        assert_eq!(&b[4..6], &2u16.to_le_bytes());
        let g = StrategyFile::from_bytes(&b).unwrap();
        assert_eq!(g, f);
        assert_eq!(g.tables.len(), 1);
        assert_eq!(g.thresholds_of(Street::Turn), Some(&thresholds()[..]));
        assert_eq!(g.thresholds_of(Street::River), Some(&thresholds()[..]));
        assert_eq!(g.thresholds_of(Street::Flop), None);
        assert_eq!(g.probabilities.len() as u64, g.tree.action_slot_count());
    }

    #[test]
    fn table_only_file_stays_version_one() {
        let b = sample(0b0011).with_thresholds(Vec::new()).to_bytes();
        assert_eq!(&b[4..6], &1u16.to_le_bytes());
    }

    #[test]
    fn wrong_threshold_count() {
        let f = sample(0b1111).with_thresholds(vec![
            (Street::Turn, thresholds()),
            (Street::River, thresholds()[1..].to_vec()),
        ]);
        assert_eq!(
            StrategyFile::from_bytes(&f.to_bytes()).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn thresholds_out_of_order_or_not_finite() {
        for bad in [f32::NAN, f32::INFINITY, 0.0] {
            let mut t = thresholds();
            t[3] = bad;
            let f = sample(0b1111)
                .with_thresholds(vec![(Street::Turn, thresholds()), (Street::River, t)]);
            assert_eq!(
                StrategyFile::from_bytes(&f.to_bytes()).err(),
                Some(StrategyError::Inconsistent),
                "{bad}"
            );
        }
    }

    #[test]
    fn unknown_bucket_kind() {
        let f = play_sample();
        let mut b = f.to_bytes();
        let tree_len = u32::from_le_bytes(b[63..67].try_into().unwrap()) as usize;
        let kind = 67 + tree_len + 4;
        assert_eq!(b[kind], 0);
        b[kind] = 2;
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn cut_body() {
        let f = sample(0b0011);
        let b = f.to_bytes();
        let tail = f.probabilities.len();
        for cut in [0, 3, 5, 40, 62, 70, b.len() - tail - 1] {
            assert_eq!(
                StrategyFile::from_bytes(&b[..cut]).err(),
                Some(StrategyError::Truncated),
                "cut {cut}"
            );
        }
    }

    #[test]
    fn short_probabilities() {
        let b = sample(0b0011).to_bytes();
        assert_eq!(
            StrategyFile::from_bytes(&b[..b.len() - 1]).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn long_probabilities() {
        let mut b = sample(0b0011).to_bytes();
        b.push(0);
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn mask_without_preflop() {
        let mut b = sample(0b0011).to_bytes();
        b[50] = 0b0010;
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn mask_with_unknown_bits() {
        let mut b = sample(0b0011).to_bytes();
        b[50] = 0b1_0011;
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn table_of_the_wrong_size() {
        let mut f = sample(0b0011);
        f.tables[0].pop();
        assert_eq!(
            StrategyFile::from_bytes(&f.to_bytes()).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn wrong_table_count() {
        let mut f = sample(0b0011);
        f.tables.clear();
        assert_eq!(
            StrategyFile::from_bytes(&f.to_bytes()).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn broken_tree() {
        let mut b = sample(0b0011).to_bytes();
        b[67] = 0;
        b[68] = 0;
        assert_eq!(
            StrategyFile::from_bytes(&b).err(),
            Some(StrategyError::Inconsistent)
        );
    }

    #[test]
    fn preflop_only_has_no_tables() {
        let f = sample(0b0001);
        assert!(f.tables.is_empty());
        let g = StrategyFile::from_bytes(&f.to_bytes()).unwrap();
        assert!(g.includes(Street::Preflop));
        assert!(!g.includes(Street::Flop));
    }

    #[test]
    fn probabilities_slice_per_infoset() {
        let f = sample(0b0011);
        let g = StrategyFile::from_bytes(&f.to_bytes()).unwrap();
        let mut pos = 0;
        for (id, node) in g.tree.nodes().iter().enumerate() {
            if let Node::Decision {
                street, actions, ..
            } = node
            {
                if !g.includes(*street) {
                    continue;
                }
                for bucket in 0..g.tree.buckets()[street.as_index()] {
                    let slice = g.probabilities(id as NodeId, bucket);
                    assert_eq!(slice, &g.probabilities[pos..pos + actions.len()]);
                    assert_eq!(slice.iter().map(|&x| u32::from(x)).sum::<u32>(), 255);
                    pos += actions.len();
                }
            }
        }
        assert_eq!(pos, g.probabilities.len());
    }

    #[test]
    #[should_panic]
    fn excluded_street_panics() {
        let f = sample(0b0001);
        let id = f
            .tree
            .nodes()
            .iter()
            .position(|n| matches!(n, Node::Decision { street, .. } if *street == Street::Flop))
            .unwrap();
        f.probabilities(id as NodeId, 0);
    }
}
