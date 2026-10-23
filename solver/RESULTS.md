# Results

One row per training run or measured checkpoint. Record the preset, the figures and the run time; change one lever
at a time (see "How to improve").

| date | preset | fingerprint prefix | stack | iterations | hours | average positive regret | LBR mbb/hand (95% interval) | match bb/100 vs call | match bb/100 vs raise | match bb/100 vs random | browser file size gzip |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|  |  |  |  |  |  |  |  |  |  |  |  |

## How to improve

The first run sets the baseline; no threshold is fixed before it. Each lever names the number it should move.

1. **More iterations.** Resume from the checkpoint for a second night. Should lower LBR mbb/hand while average
   positive regret is still falling; when it flattens, the abstraction is the limit, not the training time.
2. **More buckets.** 128 to 256 or 512 per street, within the memory limit (256 is the cap of the `u8` tables).
   Should lower LBR mbb/hand; costs memory linearly and more iterations to converge.
3. **Better buckets.** Potential-aware buckets on flop and turn (k-means over equity histograms with earth mover's
   distance) and opponent cluster hand strength (OCHS) on the river. Should lower LBR mbb/hand, usually more than
   bucket count at the same memory.
4. **Regret-based pruning.** Skip actions with very negative regret in most iterations. Should raise iterations
   per second, so more training per night.
5. **Bet sizes.** Add the sizes LBR exploits most often, drop the ones the strategy never uses. Should lower LBR
   mbb/hand; every size multiplies the tree.
6. **Vector CFR over public chance** (public chance sampling). Should lower LBR mbb/hand and average positive
   regret at equal iteration count (faster convergence per iteration); a larger rewrite of `cfr`.
7. **Deeper blueprints.** Train presets with `stack` 3000 and 4000 and pick the nearest blueprint by effective
   stack at the start of a hand. Worth it when the arena from #3 shows deep effective stacks are common and the
   `--stack 2500` and `--stack 3000` figures (LBR mbb/hand and match bb/100) are clearly worse than at 2000.
   Should close that gap; costs one night and one browser file per depth.
8. **Cloud.** Only when the levers above are spent and the memory or time limit of the local machine is the
   blocker. Should raise iterations per night and the feasible bucket count; no direct effect on LBR otherwise.
