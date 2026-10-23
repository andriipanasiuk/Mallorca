import { afterEach, describe, expect, it, vi } from 'vitest';
import { readAutoNext, readTableStyle, writeAutoNext, writeTableStyle } from './preferences.ts';

afterEach(() => {
  vi.unstubAllGlobals();
  localStorage.clear();
});

describe('table style preference', () => {
  it('defaults to the room', () => {
    expect(readTableStyle()).toBe('room');
  });

  it('remembers the simple table', () => {
    writeTableStyle('simple');
    expect(readTableStyle()).toBe('simple');
    writeTableStyle('room');
    expect(readTableStyle()).toBe('room');
  });

  it('ignores an unknown stored value', () => {
    localStorage.setItem('mallorca.tableStyle', 'neon');
    expect(readTableStyle()).toBe('room');
  });

  it('survives storage that throws', () => {
    const blocked = () => {
      throw new Error('blocked');
    };
    vi.stubGlobal('localStorage', { getItem: blocked, setItem: blocked });
    expect(() => writeTableStyle('simple')).not.toThrow();
    expect(readTableStyle()).toBe('room');
  });
});

describe('auto next hand preference', () => {
  it('is on by default', () => {
    expect(readAutoNext()).toBe(true);
  });

  it('remembers turning it off and on', () => {
    writeAutoNext(false);
    expect(readAutoNext()).toBe(false);
    writeAutoNext(true);
    expect(readAutoNext()).toBe(true);
  });

  it('stays on when storage throws', () => {
    const blocked = () => {
      throw new Error('blocked');
    };
    vi.stubGlobal('localStorage', { getItem: blocked, setItem: blocked });
    expect(() => writeAutoNext(false)).not.toThrow();
    expect(readAutoNext()).toBe(true);
  });
});
