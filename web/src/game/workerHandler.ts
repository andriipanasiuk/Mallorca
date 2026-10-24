import type { Brain } from '../brains/index.ts';
import type { WorkerReply, WorkerRequest } from './workerProtocol.ts';

export function createWorkerHandler(
  newBrain: () => Brain,
  post: (reply: WorkerReply) => void,
): (request: WorkerRequest) => void {
  let brain = newBrain();
  return (request) => {
    switch (request.kind) {
      case 'reset':
        brain = newBrain();
        break;
      case 'observe':
        for (const observation of request.observations) {
          brain.observe(observation);
        }
        break;
      case 'decide':
        try {
          post({ id: request.id, decision: brain.decide(request.legal) });
        } catch (error) {
          post({
            id: request.id,
            error: error instanceof Error ? error.message : String(error),
          });
        }
        break;
    }
  };
}
