import { useEffect, useRef } from 'preact/hooks';
import type { LogEntry, SeatIndex } from '../game/index.ts';
import { t } from './i18n.ts';
import { logLine } from './phrases.ts';

export interface HandLogProps {
  entries: LogEntry[];
  names: [string, string];
  humanSeat: SeatIndex;
}

export function HandLog({ entries, names, humanSeat }: HandLogProps) {
  const list = useRef<HTMLOListElement>(null);
  useEffect(() => {
    const node = list.current;
    if (node !== null) {
      node.scrollTop = node.scrollHeight;
    }
  }, [entries.length]);
  return (
    <aside class="hand-log" data-testid="hand-log" aria-label={t('log.title')}>
      <h2 class="hand-log-title">{t('log.title')}</h2>
      <ol class="hand-log-lines" ref={list}>
        {entries.map((entry, index) => (
          <li key={index}>{logLine(entry, names, humanSeat)}</li>
        ))}
      </ol>
    </aside>
  );
}
