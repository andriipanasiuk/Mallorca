import { afterEach, describe, expect, it, vi } from 'vitest';
import { CASH } from '../../engine/index.ts';
import type { HandStore } from './store.ts';
import { memoryStore, openHandStore } from './store.ts';
import type { HandRecord } from './record.ts';

function sample(sessionId: string, handNumber: number): HandRecord {
  return {
    version: 1,
    id: `${sessionId}-${handNumber}`,
    sessionId,
    mode: CASH,
    handNumber,
    playedAt: '2026-10-23T10:00:00.000Z',
    names: ['You', 'Bot'],
    humanSeat: 0,
    bot: { id: 'math', version: '1' },
    holeCards: [
      ['Ah', 'Kd'],
      ['Qc', 'Qd'],
    ],
    board: [],
    events: [],
    decisions: [],
  };
}

function describeStore(name: string, make: () => Promise<HandStore>) {
  describe(name, () => {
    it('lists all records by handNumber and one session on request', async () => {
      const store = await make();
      await store.add(sample('s2', 2));
      await store.add(sample('s1', 1));
      expect((await store.list()).map((r) => r.id).sort()).toEqual(['s1-1', 's2-2']);
      await store.add(sample('s1', 3));
      await store.add(sample('s1', 2));
      expect((await store.list('s1')).map((r) => r.handNumber)).toEqual([1, 2, 3]);
      expect((await store.list('s2')).map((r) => r.id)).toEqual(['s2-2']);
    });

    it('clears the store', async () => {
      const store = await make();
      await store.add(sample('s1', 1));
      await store.clear();
      expect(await store.list()).toEqual([]);
    });
  });
}

describeStore('memoryStore', () => Promise.resolve(memoryStore()));

describe('openHandStore', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('falls back to a memory store when the open request is blocked', async () => {
    const request: { onblocked?: () => void } = {};
    vi.stubGlobal('indexedDB', {
      open: () => {
        queueMicrotask(() => request.onblocked?.());
        return request;
      },
    });
    const store = await openHandStore();
    await store.add(sample('s1', 1));
    expect(await store.list()).toHaveLength(1);
  });

  it('falls back to a memory store without indexedDB', async () => {
    const store = await openHandStore();
    await store.add(sample('s1', 1));
    expect(await store.list()).toHaveLength(1);
  });
});
