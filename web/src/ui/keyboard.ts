import { useEffect } from 'preact/hooks';

export interface KeyHandlers {
  enabled: boolean;
  fold: () => void;
  passive: () => void;
  raise: () => void;
  adjust: (direction: 1 | -1) => void;
}

export interface KeyLike {
  key: string;
  ctrlKey?: boolean;
  metaKey?: boolean;
  altKey?: boolean;
}

export function handleKey(event: KeyLike, handlers: KeyHandlers): boolean {
  if (
    !handlers.enabled ||
    event.ctrlKey === true ||
    event.metaKey === true ||
    event.altKey === true
  ) {
    return false;
  }
  switch (event.key) {
    case 'f':
    case 'F':
      handlers.fold();
      return true;
    case 'c':
    case 'C':
      handlers.passive();
      return true;
    case 'r':
    case 'R':
      handlers.raise();
      return true;
    case 'ArrowRight':
    case 'ArrowUp':
      handlers.adjust(1);
      return true;
    case 'ArrowLeft':
    case 'ArrowDown':
      handlers.adjust(-1);
      return true;
    default:
      return false;
  }
}

export function useKeyboard(handlers: KeyHandlers): void {
  useEffect(() => {
    const listener = (event: KeyboardEvent) => {
      if (handleKey(event, handlers)) {
        event.preventDefault();
      }
    };
    document.addEventListener('keydown', listener);
    return () => document.removeEventListener('keydown', listener);
  }, [handlers]);
}
