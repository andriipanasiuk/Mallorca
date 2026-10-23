import type { HandRecord } from './record.ts';

export interface HandStore {
  add(record: HandRecord): Promise<void>;
  list(sessionId?: string): Promise<HandRecord[]>;
  clear(): Promise<void>;
}

const DB_NAME = 'mallorca';
const STORE = 'hands';

function byHandNumber(a: HandRecord, b: HandRecord): number {
  return a.handNumber - b.handNumber;
}

export function memoryStore(): HandStore {
  const records = new Map<string, HandRecord>();
  return {
    add(record) {
      records.set(record.id, record);
      return Promise.resolve();
    },
    list(sessionId) {
      const all = [...records.values()];
      const chosen = sessionId === undefined ? all : all.filter((r) => r.sessionId === sessionId);
      return Promise.resolve(chosen.sort(byHandNumber));
    },
    clear() {
      records.clear();
      return Promise.resolve();
    },
  };
}

function request<T>(req: IDBRequest<T>): Promise<T> {
  return new Promise((resolve, reject) => {
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error ?? new Error('indexedDB request failed'));
  });
}

function done(tx: IDBTransaction): Promise<void> {
  return new Promise((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error ?? new Error('indexedDB transaction failed'));
    tx.onabort = () => reject(tx.error ?? new Error('indexedDB transaction aborted'));
  });
}

export function indexedDbStore(): Promise<HandStore> {
  return new Promise((resolve, reject) => {
    const opening = indexedDB.open(DB_NAME, 1);
    opening.onupgradeneeded = () => {
      const store = opening.result.createObjectStore(STORE, { keyPath: 'id' });
      store.createIndex('sessionId', 'sessionId');
    };
    opening.onerror = () => reject(opening.error ?? new Error('indexedDB open failed'));
    opening.onsuccess = () => {
      const db = opening.result;
      resolve({
        async add(record) {
          const tx = db.transaction(STORE, 'readwrite');
          tx.objectStore(STORE).put(record);
          await done(tx);
        },
        async list(sessionId) {
          const store = db.transaction(STORE, 'readonly').objectStore(STORE);
          const found = await request<HandRecord[]>(
            sessionId === undefined
              ? store.getAll()
              : store.index('sessionId').getAll(IDBKeyRange.only(sessionId)),
          );
          return found.sort(byHandNumber);
        },
        async clear() {
          const tx = db.transaction(STORE, 'readwrite');
          tx.objectStore(STORE).clear();
          await done(tx);
        },
      });
    };
  });
}

export async function openHandStore(): Promise<HandStore> {
  if (typeof indexedDB === 'undefined') {
    return memoryStore();
  }
  try {
    return await indexedDbStore();
  } catch {
    return memoryStore();
  }
}
