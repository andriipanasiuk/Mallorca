import { describe, expect, it } from 'vitest';
import { signed } from './signed.ts';

describe('signed', () => {
  it('uses plus for gains, hyphen for losses and nothing for zero', () => {
    expect(signed(12.5)).toEqual({ sign: '+', value: '12.5' });
    expect(signed(-3)).toEqual({ sign: '-', value: '3.0' });
    expect(signed(0)).toEqual({ sign: '', value: '0.0' });
    expect(signed(-0.04)).toEqual({ sign: '', value: '0.0' });
  });
});
