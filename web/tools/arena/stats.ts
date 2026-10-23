export function summarize(
  nets: number[],
  bigBlind: number,
): { pairs: number; hands: number; bbPer100: number; ci95: number } {
  const n = nets.length;
  const values = nets.map((net) => (net / 2 / bigBlind) * 100);
  const mean = values.reduce((sum, v) => sum + v, 0) / n;
  if (n < 2) return { pairs: n, hands: 2 * n, bbPer100: mean, ci95: Infinity };
  const variance = values.reduce((sum, v) => sum + (v - mean) ** 2, 0) / (n - 1);
  return {
    pairs: n,
    hands: 2 * n,
    bbPer100: mean,
    ci95: (1.96 * Math.sqrt(variance)) / Math.sqrt(n),
  };
}
