import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { WorkerReply, WorkerRequest } from './workerProtocol.ts';

const legal = { fold: true, check: false, call: 10, raise: { min: 40, max: 2000 } };

interface FakeScope {
  onmessage: ((event: { data: WorkerRequest }) => void) | null;
  postMessage: (reply: WorkerReply) => void;
}

describe('bot.worker', () => {
  const replies: WorkerReply[] = [];
  const scope: FakeScope = {
    onmessage: null,
    postMessage: (reply) => {
      replies.push(reply);
    },
  };

  beforeEach(async () => {
    replies.length = 0;
    scope.onmessage = null;
    vi.resetModules();
    vi.stubGlobal('self', scope);
    await import('./bot.worker.ts');
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('installs a message handler on the worker scope', () => {
    expect(scope.onmessage).toBeTypeOf('function');
  });

  it('posts an error reply when the brain cannot decide yet', () => {
    scope.onmessage?.({ data: { kind: 'decide', id: 5, legal } });
    expect(replies).toEqual([{ id: 5, error: expect.any(String) }]);
  });

  it('posts a decision once the brain has seen the hand', () => {
    scope.onmessage?.({
      data: {
        kind: 'observe',
        observations: [
          {
            type: 'handStarted',
            handNumber: 1,
            heroOnButton: true,
            heroStack: 2000,
            villainStack: 2000,
            smallBlind: 10,
            bigBlind: 20,
          },
          { type: 'holeCards', cards: [48, 44] },
        ],
      },
    });
    scope.onmessage?.({ data: { kind: 'decide', id: 6, legal } });
    expect(replies).toEqual([
      { id: 6, decision: expect.objectContaining({ action: expect.any(Object) }) },
    ]);
  });
});
