import { createMathBrain, randomVillainModel } from '../brains/index.ts';
import type { Decision, Observation } from '../brains/index.ts';
import type { LegalActions } from '../engine/index.ts';
import type { WorkerReply, WorkerRequest } from './workerProtocol.ts';

export interface BotClient {
  observe(o: Observation[]): void;
  decide(legal: LegalActions): Promise<Decision>;
  reset(): void;
  dispose?(): void;
}

export const BOT_TIMEOUT_MS = 5000;

function newBrain() {
  return createMathBrain({ villainModel: randomVillainModel });
}

export function inProcessBot(): BotClient {
  let brain = newBrain();
  return {
    observe(observations) {
      for (const observation of observations) {
        brain.observe(observation);
      }
    },
    decide(legal) {
      const current = brain;
      return Promise.resolve().then(() => current.decide(legal));
    },
    reset() {
      brain = newBrain();
    },
  };
}

function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => {
      reject(new Error(`The bot did not answer within ${ms} ms`));
    }, ms);
    promise.then(
      (value) => {
        clearTimeout(timer);
        resolve(value);
      },
      (error: unknown) => {
        clearTimeout(timer);
        reject(error instanceof Error ? error : new Error(String(error)));
      },
    );
  });
}

const guarded = new WeakSet<BotClient>();

export function withFallback(primary: BotClient, timeoutMs = BOT_TIMEOUT_MS): BotClient {
  if (guarded.has(primary)) {
    return primary;
  }
  let active = primary;
  let usedFallback = false;
  let seen: Observation[] = [];
  let generation = 0;
  const client: BotClient = {
    observe(observations) {
      seen.push(...observations);
      active.observe(observations);
    },
    reset() {
      generation += 1;
      seen = [];
      active.reset();
    },
    async decide(legal) {
      const mine = generation;
      try {
        return await withTimeout(
          Promise.resolve().then(() => active.decide(legal)),
          timeoutMs,
        );
      } catch (error) {
        if (usedFallback || mine !== generation) {
          throw error;
        }
        usedFallback = true;
        primary.dispose?.();
        const fallback = inProcessBot();
        fallback.observe(seen);
        active = fallback;
        return fallback.decide(legal);
      }
    },
    dispose() {
      generation += 1;
      active.dispose?.();
      primary.dispose?.();
    },
  };
  guarded.add(client);
  return client;
}

function rawWorkerBot(): BotClient {
  const worker = new Worker(new URL('./bot.worker.ts', import.meta.url), { type: 'module' });
  const pending = new Map<number, { resolve: (d: Decision) => void; reject: (e: Error) => void }>();
  let nextId = 1;
  const failAll = (message: string) => {
    for (const { reject } of pending.values()) {
      reject(new Error(message));
    }
    pending.clear();
  };
  worker.onmessage = (event: MessageEvent<WorkerReply>) => {
    const reply = event.data;
    const entry = pending.get(reply.id);
    if (entry === undefined) {
      return;
    }
    pending.delete(reply.id);
    if ('decision' in reply) {
      entry.resolve(reply.decision);
    } else {
      entry.reject(new Error(reply.error));
    }
  };
  worker.onerror = () => {
    failAll('The bot worker failed');
  };
  const send = (request: WorkerRequest) => {
    worker.postMessage(request);
  };
  return {
    observe(observations) {
      send({ kind: 'observe', observations });
    },
    decide(legal) {
      const id = nextId;
      nextId += 1;
      return new Promise<Decision>((resolve, reject) => {
        pending.set(id, { resolve, reject });
        send({ kind: 'decide', id, legal });
      });
    },
    reset() {
      send({ kind: 'reset' });
    },
    dispose() {
      failAll('The bot worker was stopped');
      worker.terminate();
    },
  };
}

export function workerBot(): BotClient {
  return withFallback(rawWorkerBot());
}
