import { createMathBrain, randomVillainModel } from '../brains/index.ts';
import type { WorkerReply, WorkerRequest } from './workerProtocol.ts';
import { createWorkerHandler } from './workerHandler.ts';

interface WorkerScope {
  onmessage: ((event: MessageEvent<WorkerRequest>) => void) | null;
  postMessage(reply: WorkerReply): void;
}

const scope = self as unknown as WorkerScope;

const handle = createWorkerHandler(
  () => createMathBrain({ villainModel: randomVillainModel }),
  (reply) => {
    scope.postMessage(reply);
  },
);

scope.onmessage = (event) => {
  handle(event.data);
};
