import { describe, expect, it, vi } from 'vitest';
import { handleKey } from './keyboard.ts';

function handlers(enabled: boolean) {
  return { enabled, fold: vi.fn(), passive: vi.fn(), raise: vi.fn(), adjust: vi.fn() };
}

describe('handleKey', () => {
  it('maps f, c and r to fold, passive and raise when enabled', () => {
    const h = handlers(true);
    expect(handleKey({ key: 'f' }, h)).toBe(true);
    expect(handleKey({ key: 'C' }, h)).toBe(true);
    expect(handleKey({ key: 'r' }, h)).toBe(true);
    expect(h.fold).toHaveBeenCalledTimes(1);
    expect(h.passive).toHaveBeenCalledTimes(1);
    expect(h.raise).toHaveBeenCalledTimes(1);
  });

  it('maps arrows to slider steps', () => {
    const h = handlers(true);
    handleKey({ key: 'ArrowRight' }, h);
    handleKey({ key: 'ArrowDown' }, h);
    expect(h.adjust.mock.calls).toEqual([[1], [-1]]);
  });

  it('does nothing when disabled', () => {
    const h = handlers(false);
    expect(handleKey({ key: 'f' }, h)).toBe(false);
    expect(handleKey({ key: 'c' }, h)).toBe(false);
    expect(handleKey({ key: 'r' }, h)).toBe(false);
    expect(h.fold).not.toHaveBeenCalled();
    expect(h.passive).not.toHaveBeenCalled();
    expect(h.raise).not.toHaveBeenCalled();
  });

  it('ignores modified keys and unknown keys', () => {
    const h = handlers(true);
    expect(handleKey({ key: 'r', metaKey: true }, h)).toBe(false);
    expect(handleKey({ key: 'c', ctrlKey: true }, h)).toBe(false);
    expect(handleKey({ key: 'x' }, h)).toBe(false);
  });
});
