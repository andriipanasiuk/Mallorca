use mallorca_core::abstraction::Abstraction;
use mallorca_core::checkpoint::read_checkpoint;
use mallorca_core::fingerprint::fingerprint;
use mallorca_core::preset::Preset;
use mallorca_core::strategy::StrategyFile;
use mallorca_core::tree::{Node, Tree};
use std::path::{Path, PathBuf};
use std::process::Command;

const TINY: &str = include_str!("../../../presets/tiny.toml");

fn scratch() -> PathBuf {
    let dir = std::env::temp_dir().join(format!("mallorca-e2e-{}", std::process::id()));
    let _ = std::fs::remove_dir_all(&dir);
    std::fs::create_dir_all(&dir).unwrap();
    dir
}

fn run(args: &[&str]) -> String {
    let output = Command::new(env!("CARGO_BIN_EXE_mallorca-trainer"))
        .args(args)
        .output()
        .expect("binary starts");
    let stdout = String::from_utf8_lossy(&output.stdout).into_owned();
    assert!(
        output.status.success(),
        "{args:?} failed: {}\n{stdout}",
        String::from_utf8_lossy(&output.stderr)
    );
    stdout
}

fn path(p: &Path) -> &str {
    p.to_str().unwrap()
}

fn latest_checkpoint(dir: &Path) -> PathBuf {
    std::fs::read_dir(dir)
        .unwrap()
        .filter_map(|e| {
            let p = e.unwrap().path();
            let n: u32 = p.file_stem()?.to_str()?.parse().ok()?;
            (p.extension()? == "ckpt").then_some((n, p))
        })
        .max()
        .expect("a checkpoint exists")
        .1
}

fn check_file(file: &StrategyFile, expected: &[u8; 32], mask: u8) {
    assert_eq!(&file.fingerprint, expected);
    assert_eq!(file.streets, mask);
    let mut infosets = 0u64;
    for (id, node) in file.tree.nodes().iter().enumerate() {
        let Node::Decision { street, .. } = node else {
            continue;
        };
        if !file.includes(*street) {
            continue;
        }
        for bucket in 0..file.tree.buckets()[street.as_index()] {
            let sum: u32 = file
                .probabilities(id as u32, bucket)
                .iter()
                .map(|&p| u32::from(p))
                .sum();
            assert_eq!(sum, 255, "node {id} bucket {bucket}");
            infosets += 1;
        }
    }
    assert!(infosets > 0);
}

fn assert_prints_number(output: &str, marker: &str) {
    let line = output
        .lines()
        .find(|l| l.contains(marker))
        .unwrap_or_else(|| panic!("no {marker} line in:\n{output}"));
    assert!(
        line.split_whitespace()
            .any(|w| w.trim_start_matches('+').parse::<f64>().is_ok()),
        "no number in {line}"
    );
}

#[test]
#[ignore]
fn tiny_runs_through_every_subcommand() {
    let dir = scratch();
    let preset = dir.join("tiny.toml");
    std::fs::write(&preset, TINY).unwrap();
    let data = dir.join("data");
    std::fs::create_dir_all(&data).unwrap();
    let (preset_arg, data_arg) = (path(&preset), path(&data));

    run(&["buckets", "--preset", preset_arg, "--out", data_arg]);
    let tree_out = run(&["tree", "--preset", preset_arg]);
    assert!(tree_out.contains("action slots"));
    run(&[
        "train",
        "--preset",
        preset_arg,
        "--data",
        data_arg,
        "--iterations",
        "20000",
        "--threads",
        "2",
    ]);

    let browser = dir.join("browser.bin");
    let all = dir.join("all.bin");
    run(&[
        "export",
        "--preset",
        preset_arg,
        "--data",
        data_arg,
        "--streets",
        "browser",
        "--out",
        path(&browser),
    ]);
    run(&[
        "export",
        "--preset",
        preset_arg,
        "--data",
        data_arg,
        "--streets",
        "all",
        "--out",
        path(&all),
    ]);

    let preset_value = Preset::from_toml(TINY).unwrap();
    let tree = Tree::build(&preset_value);
    let read = |name: &str| std::fs::read(data.join(name)).unwrap();
    let abstraction = Abstraction::new([read("flop.bkt"), read("turn.bkt"), read("river.bkt")]);
    let expected = fingerprint(&preset_value, &tree, &abstraction);
    let checkpoint = std::fs::read(latest_checkpoint(&data.join("checkpoints"))).unwrap();
    let (header, _) = read_checkpoint(&checkpoint, &expected).expect("checkpoint fingerprint");
    assert!(header.iterations >= 20000);

    let browser_file = StrategyFile::from_bytes(&std::fs::read(&browser).unwrap()).unwrap();
    let all_file = StrategyFile::from_bytes(&std::fs::read(&all).unwrap()).unwrap();
    check_file(&browser_file, &header.fingerprint, 0b0011);
    check_file(&all_file, &header.fingerprint, 0b1111);
    assert_eq!(browser_file.iterations, header.iterations);

    let lbr = run(&[
        "lbr",
        "--strategy",
        path(&all),
        "--hands",
        "2000",
        "--threads",
        "2",
    ]);
    assert_prints_number(&lbr, "mbb/hand");

    let matches = run(&[
        "match",
        "--strategy",
        path(&all),
        "--opponent",
        "all",
        "--deals",
        "2000",
    ]);
    for name in ["call", "raise", "random"] {
        assert_prints_number(&matches, &format!("{name}: "));
    }

    let _ = std::fs::remove_dir_all(&dir);
}
