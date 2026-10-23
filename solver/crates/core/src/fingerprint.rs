use crate::abstraction::Abstraction;
use crate::preset::Preset;
use crate::tree::{Street, Tree};
use sha2::{Digest, Sha256};

pub fn fingerprint(preset: &Preset, tree: &Tree, abstraction: &Abstraction) -> [u8; 32] {
    let mut hasher = Sha256::new();
    hasher.update(preset.canonical_toml().as_bytes());
    hasher.update(tree.serialize());
    for street in [Street::Flop, Street::Turn, Street::River] {
        hasher.update(abstraction.table_bytes(street));
    }
    hasher.finalize().into()
}

#[cfg(test)]
mod tests {
    use super::*;

    const TINY: &str = include_str!("../../../presets/tiny.toml");

    fn tables() -> [Vec<u8>; 3] {
        [vec![0, 1, 2, 3], vec![4, 5, 6], vec![7, 0, 1, 2, 3]]
    }

    fn of(preset: &Preset, tables: [Vec<u8>; 3]) -> [u8; 32] {
        fingerprint(preset, &Tree::build(preset), &Abstraction::new(tables))
    }

    #[test]
    fn same_inputs_give_same_fingerprint() {
        let p = Preset::from_toml(TINY).unwrap();
        assert_eq!(of(&p, tables()), of(&p, tables()));
    }

    #[test]
    fn preset_size_changes_fingerprint() {
        let p = Preset::from_toml(TINY).unwrap();
        let mut q = p.clone();
        q.postflop.first_bet_pot = vec![0.75];
        assert_ne!(of(&p, tables()), of(&q, tables()));
    }

    #[test]
    fn tree_node_changes_fingerprint() {
        let p = Preset::from_toml(TINY).unwrap();
        let mut q = p.clone();
        q.stack = 420;
        let abstraction = Abstraction::new(tables());
        let tree = Tree::build(&p);
        let other = Tree::build(&q);
        assert_ne!(tree.serialize(), other.serialize());
        assert_ne!(
            fingerprint(&p, &tree, &abstraction),
            fingerprint(&p, &other, &abstraction)
        );
    }

    #[test]
    fn bucket_byte_changes_fingerprint() {
        let p = Preset::from_toml(TINY).unwrap();
        let base = of(&p, tables());
        for street in 0..3 {
            let mut t = tables();
            t[street][1] ^= 1;
            assert_ne!(base, of(&p, t), "street {street}");
        }
    }
}
