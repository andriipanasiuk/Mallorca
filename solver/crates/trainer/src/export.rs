use crate::train::{latest_checkpoint, load_abstraction, load_checkpoint};
use crate::tree::load_preset;
use clap::{Args, ValueEnum};
use flate2::write::GzEncoder;
use flate2::Compression;
use mallorca_core::fingerprint::fingerprint;
use mallorca_core::strategy::{quantize, StrategyFile, StrategyHeader};
use mallorca_core::tree::{Node, Street, Tree};
use std::io::Write;
use std::path::{Path, PathBuf};

#[derive(Clone, Copy, ValueEnum)]
pub enum StreetSet {
    Browser,
    All,
}

impl StreetSet {
    fn mask(self) -> u8 {
        match self {
            StreetSet::Browser => 0b0011,
            StreetSet::All => 0b1111,
        }
    }
}

#[derive(Args)]
pub struct ExportArgs {
    #[arg(long)]
    pub preset: PathBuf,
    #[arg(long)]
    pub data: PathBuf,
    #[arg(long)]
    pub checkpoint: Option<u32>,
    #[arg(long, value_enum, default_value = "browser")]
    pub streets: StreetSet,
    #[arg(long)]
    pub out: PathBuf,
}

pub fn build(
    preset_path: &Path,
    data: &Path,
    checkpoint: Option<u32>,
    streets: StreetSet,
) -> Result<StrategyFile, String> {
    let preset = load_preset(&preset_path.to_path_buf())?;
    let tree = Tree::build(&preset);
    let abstraction = load_abstraction(&preset, data)?;
    let fp = fingerprint(&preset, &tree, &abstraction);
    let dir = data.join("checkpoints");
    let n = match checkpoint {
        Some(n) => n,
        None => latest_checkpoint(&dir)?
            .ok_or_else(|| format!("no checkpoints in {}", dir.display()))?,
    };
    let loaded = load_checkpoint(&dir, n, &fp, tree.action_slot_count())?;
    let mask = streets.mask();
    let included = |street: Street| mask & (1 << street.as_index()) != 0;
    let mut probabilities = Vec::new();
    for (id, node) in tree.nodes().iter().enumerate() {
        let Node::Decision {
            street, actions, ..
        } = node
        else {
            continue;
        };
        if !included(*street) {
            continue;
        }
        for bucket in 0..tree.buckets()[street.as_index()] {
            let offset = tree.offset(id as u32, bucket);
            let average = loaded.storage.average_strategy(offset, actions.len());
            probabilities.extend(quantize(&average));
        }
    }
    let tables = [Street::Flop, Street::Turn, Street::River]
        .into_iter()
        .filter(|&street| included(street))
        .map(|street| abstraction.table_bytes(street).to_vec())
        .collect();
    let header = StrategyHeader {
        fingerprint: fp,
        small_blind: preset.small_blind,
        big_blind: preset.big_blind,
        stack: preset.stack,
        streets: mask,
        iterations: loaded.iterations,
        checkpoint: loaded.checkpoint,
    };
    Ok(StrategyFile::new(header, tree, tables, probabilities))
}

fn gzip_size(bytes: &[u8]) -> Result<usize, String> {
    let mut encoder = GzEncoder::new(Vec::new(), Compression::default());
    encoder
        .write_all(bytes)
        .map_err(|e| format!("cannot compress: {e}"))?;
    let out = encoder
        .finish()
        .map_err(|e| format!("cannot compress: {e}"))?;
    Ok(out.len())
}

pub fn run(args: &ExportArgs) -> Result<(), String> {
    let file = build(&args.preset, &args.data, args.checkpoint, args.streets)?;
    let bytes = file.to_bytes();
    let tmp = args.out.with_extension("tmp");
    let failed = |e: std::io::Error| format!("cannot write {}: {e}", args.out.display());
    std::fs::write(&tmp, &bytes).map_err(failed)?;
    std::fs::rename(&tmp, &args.out).map_err(failed)?;
    let gzipped = gzip_size(&bytes)?;
    println!(
        "checkpoint {}: {} iterations, streets mask {:#06b}",
        file.checkpoint, file.iterations, file.streets
    );
    println!(
        "wrote {}: {} bytes raw ({:.2} MB), {gzipped} bytes gzipped ({:.2} MB)",
        args.out.display(),
        bytes.len(),
        bytes.len() as f64 / 1e6,
        gzipped as f64 / 1e6
    );
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::train::tests::{data_dir, go, session};
    use mallorca_core::strategy::StrategyError;

    fn trained(name: &str, iterations: u64) -> PathBuf {
        let dir = data_dir(name);
        go(&session(&dir, Some(iterations), 3, Some(1))).unwrap();
        dir
    }

    fn export(dir: &Path, streets: StreetSet) -> Result<StrategyFile, String> {
        build(&dir.join("tiny.toml"), dir, None, streets)
    }

    #[test]
    fn browser_export_holds_preflop_and_flop_only() {
        let dir = trained("export-browser", 200);
        let file = export(&dir, StreetSet::Browser).unwrap();
        assert_eq!(file.streets, 0b0011);
        assert_eq!(file.tables.len(), 1);
        assert_eq!(file.tables[0], std::fs::read(dir.join("flop.bkt")).unwrap());
        assert_eq!((file.iterations, file.checkpoint), (200, 1));
        assert_eq!(
            (file.small_blind, file.big_blind, file.stack),
            (10, 20, 400)
        );
        let ckpt = std::fs::read(dir.join("checkpoints/1.ckpt")).unwrap();
        assert_eq!(&file.fingerprint[..], &ckpt[6..38]);
        let mut slices = 0;
        for (id, node) in file.tree.nodes().iter().enumerate() {
            if let Node::Decision { street, .. } = node {
                if file.includes(*street) {
                    for bucket in 0..file.tree.buckets()[street.as_index()] {
                        let p = file.probabilities(id as u32, bucket);
                        assert_eq!(p.iter().map(|&x| u32::from(x)).sum::<u32>(), 255);
                        slices += 1;
                    }
                } else {
                    assert!(matches!(street, Street::Turn | Street::River));
                }
            }
        }
        assert!(slices > 0);
        let again = StrategyFile::from_bytes(&file.to_bytes()).unwrap();
        assert_eq!(again, file);
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn export_matches_the_average_strategy() {
        let dir = trained("export-average", 300);
        let file = export(&dir, StreetSet::Browser).unwrap();
        let preset = load_preset(&dir.join("tiny.toml")).unwrap();
        let tree = Tree::build(&preset);
        let abstraction = load_abstraction(&preset, &dir).unwrap();
        let fp = fingerprint(&preset, &tree, &abstraction);
        let loaded =
            load_checkpoint(&dir.join("checkpoints"), 1, &fp, tree.action_slot_count()).unwrap();
        let root = tree.root();
        let Node::Decision { actions, .. } = tree.node(root) else {
            panic!("root is a decision");
        };
        for bucket in [0u16, 50, 168] {
            let average = loaded
                .storage
                .average_strategy(tree.offset(root, bucket), actions.len());
            assert_eq!(file.probabilities(root, bucket), quantize(&average));
        }
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn all_streets_export_has_every_table() {
        let dir = trained("export-all", 100);
        let file = export(&dir, StreetSet::All).unwrap();
        assert_eq!(file.streets, 0b1111);
        assert_eq!(file.tables.len(), 3);
        for (table, name) in file.tables.iter().zip(["flop", "turn", "river"]) {
            assert_eq!(
                table,
                &std::fs::read(dir.join(format!("{name}.bkt"))).unwrap()
            );
        }
        assert_eq!(
            file.probabilities.len() as u64,
            file.tree.action_slot_count()
        );
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn explicit_checkpoint_is_chosen() {
        let dir = trained("export-explicit", 100);
        go(&session(&dir, Some(100), 3, Some(1))).unwrap();
        let first = build(&dir.join("tiny.toml"), &dir, Some(1), StreetSet::Browser).unwrap();
        let latest = export(&dir, StreetSet::Browser).unwrap();
        assert_eq!((first.checkpoint, first.iterations), (1, 100));
        assert_eq!((latest.checkpoint, latest.iterations), (2, 200));
        let missing = build(&dir.join("tiny.toml"), &dir, Some(9), StreetSet::Browser);
        assert!(missing.err().unwrap().contains("9.ckpt"));
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn checkpoint_of_another_preset_is_refused() {
        let dir = trained("export-foreign", 100);
        let text = std::fs::read_to_string(dir.join("tiny.toml")).unwrap();
        std::fs::write(
            dir.join("other.toml"),
            text.replace("stack = 400", "stack = 500"),
        )
        .unwrap();
        let err = build(&dir.join("other.toml"), &dir, None, StreetSet::Browser)
            .err()
            .unwrap();
        assert!(err.contains("another preset"), "{err}");
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn no_checkpoint_is_an_error() {
        let dir = data_dir("export-none");
        std::fs::create_dir_all(dir.join("checkpoints")).unwrap();
        let err = export(&dir, StreetSet::Browser).err().unwrap();
        assert!(err.contains("no checkpoints"), "{err}");
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn written_file_is_readable_and_gzip_is_smaller() {
        let dir = trained("export-write", 100);
        let out = dir.join("strategy.bin");
        run(&ExportArgs {
            preset: dir.join("tiny.toml"),
            data: dir.clone(),
            checkpoint: None,
            streets: StreetSet::Browser,
            out: out.clone(),
        })
        .unwrap();
        let bytes = std::fs::read(&out).unwrap();
        assert!(StrategyFile::from_bytes(&bytes).is_ok());
        assert!(gzip_size(&bytes).unwrap() < bytes.len());
        assert!(!dir.join("strategy.tmp").exists());
        let mut cut = bytes.clone();
        cut.truncate(10);
        assert_eq!(
            StrategyFile::from_bytes(&cut).err(),
            Some(StrategyError::Truncated)
        );
        std::fs::remove_dir_all(dir).unwrap();
    }
}
