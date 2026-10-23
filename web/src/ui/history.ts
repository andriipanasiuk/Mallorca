import { toPokerStars } from '../game/index.ts';
import type { HandStore } from '../game/index.ts';
import { download } from './download.ts';

export type HistoryFormat = 'json' | 'text';

export async function exportHistory(
  store: HandStore,
  sessionId: string | null,
  format: HistoryFormat,
): Promise<void> {
  try {
    const records = sessionId === null ? await store.list() : await store.list(sessionId);
    const name = `mallorca-${sessionId ?? 'history'}`;
    if (format === 'json') {
      download(`${name}.json`, JSON.stringify(records, null, 2), 'application/json');
    } else {
      download(`${name}.txt`, toPokerStars(records), 'text/plain');
    }
  } catch {
    return;
  }
}

export async function clearHistory(store: HandStore): Promise<void> {
  try {
    await store.clear();
  } catch {
    return;
  }
}
