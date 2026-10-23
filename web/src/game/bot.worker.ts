import { createMathBrain, randomVillainModel } from '../brains/index.ts';
import type { WorkerReply, WorkerRequest } from './workerProtocol.ts';

interface WorkerScope {
  onmessage: ((event: MessageEvent<WorkerRequest>) => void) | null;
  postMessage(reply: WorkerReply): void;
}

const scope = self as unknown as WorkerScope;

let brain = createMathBrain({ villainModel: randomVillainModel });

scope.onmessage = (event) => {
  const request = event.data;
  switch (request.kind) {
    case 'reset':
      brain = createMathBrain({ villainModel: randomVillainModel });
      break;
    case 'observe':
      for (const observation of request.observations) {
        brain.observe(observation);
      }
      break;
    case 'decide':
      try {
        scope.postMessage({ id: request.id, decision: brain.decide(request.legal) });
      } catch (error) {
        scope.postMessage({
          id: request.id,
          error: error instanceof Error ? error.message : String(error),
        });
      }
      break;
  }
};
