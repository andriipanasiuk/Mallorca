export interface Signed {
  sign: string;
  value: string;
}

export function signed(amount: number): Signed {
  const rounded = Math.round(Math.abs(amount) * 10) / 10;
  const sign = rounded === 0 ? '' : amount > 0 ? '+' : '-';
  return { sign, value: rounded.toFixed(1) };
}
