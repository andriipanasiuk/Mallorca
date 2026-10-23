use clap::Args;
use mallorca_core::cfr::SLOT_BYTES;
use mallorca_core::preset::Preset;
use mallorca_core::tree::{Node, Tree};
use std::path::PathBuf;

#[derive(Args)]
pub struct TreeArgs {
    #[arg(long)]
    pub preset: PathBuf,
}

pub fn load_preset(path: &PathBuf) -> Result<Preset, String> {
    let text = std::fs::read_to_string(path)
        .map_err(|e| format!("cannot read {}: {e}", path.display()))?;
    Preset::from_toml(&text).map_err(|e| format!("{}: {e}", path.display()))
}

pub fn run(args: &TreeArgs) -> Result<(), String> {
    let preset = load_preset(&args.preset)?;
    let tree = Tree::build(&preset);
    let mut decisions = [0u64; 4];
    let mut folds = 0u64;
    let mut showdowns = 0u64;
    for node in tree.nodes() {
        match node {
            Node::Decision { street, .. } => decisions[street.as_index()] += 1,
            Node::Fold { .. } => folds += 1,
            Node::Showdown { .. } => showdowns += 1,
        }
    }
    let slots = tree.action_slot_count();
    let bytes = slots * SLOT_BYTES as u64;
    println!("preset: {}", preset.name);
    println!("buckets: {:?}", tree.buckets());
    println!("nodes: {}", tree.nodes().len());
    println!(
        "decision nodes: preflop {}, flop {}, turn {}, river {}",
        decisions[0], decisions[1], decisions[2], decisions[3]
    );
    println!("terminal nodes: fold {folds}, showdown {showdowns}");
    println!("infosets: {}", tree.infoset_count());
    println!("action slots: {slots}");
    println!(
        "memory estimate: {bytes} bytes ({:.3} GB)",
        bytes as f64 / 1e9
    );
    Ok(())
}
