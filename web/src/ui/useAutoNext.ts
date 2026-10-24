import { useEffect } from 'preact/hooks';
import type { Session, ViewModel } from '../game/index.ts';
import { nextHandDelay } from './autoNext.ts';

export function useAutoNext(
  session: Session,
  vm: ViewModel | null,
  enabled: boolean,
): number | null {
  const delay = enabled && vm !== null ? nextHandDelay(vm) : null;
  const handNumber = vm?.handNumber ?? null;
  useEffect(() => {
    if (delay === null) {
      return;
    }
    const timer = setTimeout(() => {
      if (session.getState().phase === 'handOver') {
        void session.getState().nextHand();
      }
    }, delay);
    return () => clearTimeout(timer);
  }, [session, delay, handNumber]);
  return delay;
}
