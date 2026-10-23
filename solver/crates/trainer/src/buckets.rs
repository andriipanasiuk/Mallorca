use crate::tree::load_preset;
use clap::Args;
use mallorca_core::abstraction::{aggregate_hs2, percentile_buckets, river_strength};
use mallorca_core::cards::Card;
use mallorca_core::iso::{board_indexer, street_indexers};
use mallorca_core::tree::Street;
use std::fs::File;
use std::io::{BufWriter, Write};
use std::ops::Range;
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicU32, AtomicUsize, Ordering};
use std::time::Instant;

const CHUNK: usize = 512;

#[derive(Args)]
pub struct BucketsArgs {
    #[arg(long)]
    pub preset: PathBuf,
    #[arg(long)]
    pub out: PathBuf,
}

fn parallel_chunks<F>(label: &str, total: usize, threads: usize, work: F)
where
    F: Fn(Range<usize>) + Sync,
{
    let next = AtomicUsize::new(0);
    let done = AtomicUsize::new(0);
    let printed = AtomicUsize::new(0);
    let start = Instant::now();
    std::thread::scope(|scope| {
        for _ in 0..threads.max(1) {
            scope.spawn(|| loop {
                let from = next.fetch_add(CHUNK, Ordering::Relaxed);
                if from >= total {
                    break;
                }
                let to = (from + CHUNK).min(total);
                work(from..to);
                let finished = done.fetch_add(to - from, Ordering::Relaxed) + (to - from);
                let step = finished * 20 / total;
                if printed.fetch_max(step, Ordering::Relaxed) < step {
                    println!(
                        "{label}: {}% ({:.0}s)",
                        step * 5,
                        start.elapsed().as_secs_f64()
                    );
                }
            });
        }
    });
}

fn atomic_cells(n: usize, value: f32) -> Vec<AtomicU32> {
    (0..n).map(|_| AtomicU32::new(value.to_bits())).collect()
}

fn into_floats(cells: Vec<AtomicU32>) -> Vec<f32> {
    cells
        .into_iter()
        .map(|c| f32::from_bits(c.into_inner()))
        .collect()
}

fn write_bytes(path: &Path, bytes: &[u8]) -> Result<(), String> {
    std::fs::write(path, bytes).map_err(|e| format!("cannot write {}: {e}", path.display()))
}

fn write_floats(path: &Path, values: &[f32]) -> Result<(), String> {
    let file = File::create(path).map_err(|e| format!("cannot write {}: {e}", path.display()))?;
    let mut writer = BufWriter::new(file);
    for chunk in values.chunks(1 << 16) {
        let mut buf = Vec::with_capacity(chunk.len() * 4);
        for v in chunk {
            buf.extend_from_slice(&v.to_le_bytes());
        }
        writer
            .write_all(&buf)
            .map_err(|e| format!("cannot write {}: {e}", path.display()))?;
    }
    writer
        .flush()
        .map_err(|e| format!("cannot write {}: {e}", path.display()))
}

pub fn run(args: &BucketsArgs) -> Result<(), String> {
    let preset = load_preset(&args.preset)?;
    let threads = preset.threads;
    std::fs::create_dir_all(&args.out)
        .map_err(|e| format!("cannot create {}: {e}", args.out.display()))?;
    let started = Instant::now();
    let indexers = street_indexers();
    let boards = board_indexer(5);
    let board_count = boards.size(0) as usize;
    let river_count = indexers[3].size(1) as usize;
    let turn_count = indexers[2].size(1) as usize;
    let flop_count = indexers[1].size(1) as usize;

    println!("river: {board_count} canonical boards, {river_count} classes, {threads} threads");
    let river_cells = atomic_cells(river_count, f32::NAN);
    parallel_chunks("river", board_count, threads, |range| {
        for b in range {
            let cards = boards.unindex(0, b as u64);
            let board: [Card; 5] = cards.try_into().expect("five board cards");
            for (index, hs) in river_strength(&board) {
                river_cells[index as usize].store(hs.to_bits(), Ordering::Relaxed);
            }
        }
    });
    let river_hs = into_floats(river_cells);
    if let Some(missing) = river_hs.iter().position(|v| v.is_nan()) {
        return Err(format!("river class {missing} was never computed"));
    }

    println!("turn: {turn_count} classes");
    let turn_cells = atomic_cells(turn_count, 0.0);
    parallel_chunks("turn", turn_count, threads, |range| {
        for i in range {
            let v = aggregate_hs2(&indexers, Street::Turn, i as u64, &river_hs);
            turn_cells[i].store(v.to_bits(), Ordering::Relaxed);
        }
    });
    let turn_values = into_floats(turn_cells);

    println!("flop: {flop_count} classes");
    let flop_cells = atomic_cells(flop_count, 0.0);
    parallel_chunks("flop", flop_count, threads, |range| {
        for i in range {
            let v = aggregate_hs2(&indexers, Street::Flop, i as u64, &turn_values);
            flop_cells[i].store(v.to_bits(), Ordering::Relaxed);
        }
    });
    let flop_values = into_floats(flop_cells);

    let tables = [
        ("flop.bkt", &flop_values, preset.buckets[0]),
        ("turn.bkt", &turn_values, preset.buckets[1]),
        ("river.bkt", &river_hs, preset.buckets[2]),
    ];
    for (name, values, buckets) in tables {
        let table = percentile_buckets(values, buckets);
        write_bytes(&args.out.join(name), &table)?;
        println!("wrote {name}: {} bytes", table.len());
    }
    write_floats(&args.out.join("river_hs.f32"), &river_hs)?;
    println!("wrote river_hs.f32: {} bytes", river_hs.len() * 4);
    println!("total {:.1}s", started.elapsed().as_secs_f64());
    Ok(())
}
