use crate::measure::load_strategy;
use clap::Args;
use mallorca_core::measure::lbr::{lbr_hands, summarize, LbrConfig, LbrResult};
use mallorca_core::measure::policy::StrategyPolicy;
use mallorca_core::rng::Rng;
use std::path::PathBuf;
use std::time::Instant;

#[derive(Args)]
pub struct LbrArgs {
    #[arg(long)]
    pub strategy: PathBuf,
    #[arg(long)]
    pub hands: u64,
    #[arg(long, default_value_t = 1)]
    pub seed: u64,
    #[arg(long, default_value_t = 2000)]
    pub stack: u32,
    #[arg(long)]
    pub threads: Option<usize>,
}

fn default_threads() -> usize {
    std::thread::available_parallelism().map_or(1, |n| n.get())
}

pub fn result(args: &LbrArgs) -> Result<LbrResult, String> {
    if args.hands == 0 {
        return Err("hands must be at least 1".to_string());
    }
    let threads = args.threads.unwrap_or_else(default_threads);
    if threads == 0 {
        return Err("threads must be positive".to_string());
    }
    let (file, abstraction) = load_strategy(&args.strategy)?;
    if args.stack < file.stack {
        return Err(format!(
            "stack {} is below the strategy's {}",
            args.stack, file.stack
        ));
    }
    let policy = StrategyPolicy {
        file: &file,
        abstraction: &abstraction,
    };
    let mut seeds = Rng::new(args.seed);
    let count = threads as u64;
    let configs: Vec<LbrConfig> = (0..count)
        .map(|t| {
            let hands = args.hands / count + u64::from(t < args.hands % count);
            LbrConfig::new(hands, seeds.next_u64(), args.stack)
        })
        .filter(|c| c.hands > 0)
        .collect();
    let tree = &file.tree;
    let policy = &policy;
    let parts: Vec<Vec<(usize, i64)>> = std::thread::scope(|scope| {
        let workers: Vec<_> = configs
            .iter()
            .map(|config| scope.spawn(move || lbr_hands(tree, policy, config)))
            .collect();
        workers
            .into_iter()
            .map(|w| w.join().expect("lbr worker panicked"))
            .collect()
    });
    Ok(summarize(tree, &parts))
}

pub fn line(r: &LbrResult, hands: u64) -> String {
    format!(
        "lbr: {:+.2} mbb/hand ± {:.2} over {hands} hands (button {:+.2}, big blind {:+.2})",
        r.mbb_per_hand, r.ci95, r.by_seat[0], r.by_seat[1]
    )
}

pub fn run(args: &LbrArgs) -> Result<(), String> {
    let started = Instant::now();
    let r = result(args)?;
    let seconds = started.elapsed().as_secs_f64();
    println!("{}", line(&r, args.hands));
    println!(
        "{} hands in {seconds:.1}s ({:.0} hands/s)",
        args.hands,
        args.hands as f64 / seconds
    );
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::buckets::{self, BucketsArgs};
    use crate::export::{build, StreetSet};
    use crate::train::tests::{data_dir, go, session};
    use std::path::Path;
    use std::sync::OnceLock;

    const TINY: &str = include_str!("../../../presets/tiny.toml");
    const TABLES: [&str; 3] = ["flop.bkt", "turn.bkt", "river.bkt"];

    fn export_to(dir: &Path) -> PathBuf {
        let file = build(&dir.join("tiny.toml"), dir, None, StreetSet::All).unwrap();
        let path = dir.join("strategy.bin");
        std::fs::write(&path, file.to_bytes()).unwrap();
        path
    }

    fn exported(name: &str, iterations: u64) -> (PathBuf, PathBuf) {
        let dir = data_dir(name);
        go(&session(&dir, Some(iterations), 3, Some(1))).unwrap();
        let path = export_to(&dir);
        (dir, path)
    }

    fn args(strategy: &Path, hands: u64, stack: u32) -> LbrArgs {
        LbrArgs {
            strategy: strategy.to_path_buf(),
            hands,
            seed: 5,
            stack,
            threads: Some(2),
        }
    }

    #[test]
    fn lbr_runs_on_an_exported_file_and_is_reproducible() {
        let (dir, path) = exported("lbr-run", 200);
        let a = result(&args(&path, 30, 400)).unwrap();
        let b = result(&args(&path, 30, 400)).unwrap();
        assert_eq!(a, b);
        assert!(a.mbb_per_hand.is_finite() && a.ci95 > 0.0);
        assert!(result(&args(&path, 10, 600)).is_ok());
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn threads_are_deterministic_for_a_seed_and_thread_count() {
        let (dir, path) = exported("lbr-threads", 200);
        let with = |threads: usize| {
            result(&LbrArgs {
                threads: Some(threads),
                ..args(&path, 41, 400)
            })
            .unwrap()
        };
        let three = with(3);
        assert_eq!(three, with(3));
        assert_ne!(three, with(1));
        assert!(three.mbb_per_hand.is_finite() && three.ci95 > 0.0);
        let many = with(64);
        assert_eq!(many, with(64));
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn bad_arguments_are_errors() {
        let (dir, path) = exported("lbr-bad", 100);
        let err = result(&args(&path, 0, 400)).err().unwrap();
        assert!(err.contains("hands"), "{err}");
        let err = result(&LbrArgs {
            threads: Some(0),
            ..args(&path, 10, 400)
        })
        .err()
        .unwrap();
        assert!(err.contains("threads"), "{err}");
        let err = result(&args(&path, 10, 300)).err().unwrap();
        assert!(err.contains("400"), "{err}");
        let err = result(&args(Path::new("/nonexistent/s.bin"), 10, 400))
            .err()
            .unwrap();
        assert!(err.contains("/nonexistent/s.bin"), "{err}");
        std::fs::remove_dir_all(dir).unwrap();
    }

    #[test]
    fn line_shows_mbb_interval_hands_and_seats() {
        let r = LbrResult {
            mbb_per_hand: 1234.567,
            ci95: 89.01,
            by_seat: [1500.0, -969.25],
        };
        assert_eq!(
            line(&r, 20_000),
            "lbr: +1234.57 mbb/hand ± 89.01 over 20000 hands (button +1500.00, big blind -969.25)"
        );
    }

    fn real_tables() -> &'static Path {
        static DIR: OnceLock<PathBuf> = OnceLock::new();
        DIR.get_or_init(|| {
            let root = Path::new(env!("CARGO_MANIFEST_DIR")).join("../../target/tmp");
            let dir = root.join("trainer-real-tables");
            if TABLES.iter().all(|t| dir.join(t).exists()) {
                return dir;
            }
            let staging = root.join("trainer-real-tables.tmp");
            let _ = std::fs::remove_dir_all(&staging);
            std::fs::create_dir_all(&staging).unwrap();
            let threads = std::thread::available_parallelism().map_or(2, |n| n.get());
            let preset = TINY.replace("threads = 2", &format!("threads = {threads}"));
            let preset_path = staging.join("tiny.toml");
            std::fs::write(&preset_path, preset).unwrap();
            buckets::run(&BucketsArgs {
                preset: preset_path,
                out: staging.clone(),
            })
            .unwrap();
            std::fs::remove_file(staging.join("river_hs.f32")).unwrap();
            let _ = std::fs::remove_dir_all(&dir);
            std::fs::rename(&staging, &dir).unwrap();
            dir
        })
    }

    fn trained_on_real_tables(name: &str, iterations: u64) -> PathBuf {
        let dir = data_dir(name);
        for table in TABLES {
            std::fs::remove_file(dir.join(table)).unwrap();
            std::fs::hard_link(real_tables().join(table), dir.join(table)).unwrap();
        }
        go(&session(&dir, Some(iterations), 3, Some(1))).unwrap();
        dir
    }

    #[test]
    #[ignore]
    fn more_iterations_give_a_lower_lbr_on_real_tables() {
        let mut figures = Vec::new();
        for (name, iterations) in [("lbr-20k", 20_000u64), ("lbr-200k", 200_000)] {
            let dir = trained_on_real_tables(name, iterations);
            let path = export_to(&dir);
            let started = std::time::Instant::now();
            let r = result(&LbrArgs {
                strategy: path,
                hands: 20_000,
                seed: 11,
                stack: 400,
                threads: None,
            })
            .unwrap();
            let seconds = started.elapsed().as_secs_f64();
            println!(
                "{iterations} iterations: {} ({:.0} hands/s)",
                line(&r, 20_000),
                20_000.0 / seconds
            );
            figures.push(r);
            std::fs::remove_dir_all(dir).unwrap();
        }
        let (early, late) = (figures[0], figures[1]);
        assert!(late.mbb_per_hand > 0.0, "{late:?}");
        assert!(
            early.mbb_per_hand > late.mbb_per_hand,
            "{early:?} vs {late:?}"
        );
    }
}
