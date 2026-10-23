import { describe, expect, it } from 'vitest';
import type { Brain, Decision, Observation } from '../brains/index.ts';
import type { WorkerReply } from './workerProtocol.ts';
import { createWorkerHandler } from './workerHandler.ts';

const legal = { fold: true, check: false, call: 10, raise: { min: 40, max: 2000 } };

const observation: Observation = { type: 'holeCards', cards: [48, 44] };

const decision: Decision = {
  action: { kind: 'call' },
  solverChoice: 'passive',
  evByAction: { fold: 0, passive: 0, aggressive: 0 },
};

function fakeBrain(decide: Brain['decide'] = () => decision) {
  const observed: Observation[] = [];
  const brain: Brain = {
    observe: (o) => {
      observed.push(o);
    },
    decide,
  };
  return { brain, observed };
}

describe('createWorkerHandler', () => {
  it('feeds every observation to the brain', () => {
    const { brain, observed } = fakeBrain();
    const handle = createWorkerHandler(() => brain, () => undefined);
    handle({ kind: 'observe', observations: [observation, observation] });
    expect(observed).toHaveLength(2);
  });

  it('replies to decide with the decision and the request id', () => {
    const { brain } = fakeBrain();
    const replies: WorkerReply[] = [];
    const handle = createWorkerHandler(() => brain, (reply) => replies.push(reply));
    handle({ kind: 'decide', id: 7, legal });
    expect(replies).toEqual([{ id: 7, decision }]);
  });

  it('replies with the error message when the brain throws an Error', () => {
    const { brain } = fakeBrain(() => {
      throw new Error('no cards');
    });
    const replies: WorkerReply[] = [];
    const handle = createWorkerHandler(() => brain, (reply) => replies.push(reply));
    handle({ kind: 'decide', id: 3, legal });
    expect(replies).toEqual([{ id: 3, error: 'no cards' }]);
  });

  it('replies with the stringified value when the brain throws a non-Error', () => {
    const { brain } = fakeBrain(() => {
      throw 'boom';
    });
    const replies: WorkerReply[] = [];
    const handle = createWorkerHandler(() => brain, (reply) => replies.push(reply));
    handle({ kind: 'decide', id: 4, legal });
    expect(replies).toEqual([{ id: 4, error: 'boom' }]);
  });

  it('builds a fresh brain on reset and sends no reply', () => {
    const first = fakeBrain();
    const second = fakeBrain();
    const brains = [first.brain, second.brain];
    const replies: WorkerReply[] = [];
    const handle = createWorkerHandler(
      () => brains.shift() as Brain,
      (reply) => replies.push(reply),
    );
    handle({ kind: 'observe', observations: [observation] });
    handle({ kind: 'reset' });
    handle({ kind: 'observe', observations: [observation] });
    expect(first.observed).toHaveLength(1);
    expect(second.observed).toHaveLength(1);
    expect(replies).toEqual([]);
  });
});
