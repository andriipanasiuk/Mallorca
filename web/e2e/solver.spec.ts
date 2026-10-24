import { byId, expect, FAST_URL, test, waitForWaypoint } from './fixtures.ts';

const SOLVER_TIMEOUT_MS = 120_000;

test('the solver loads in the worker and plays cash hands', async ({ page, context }) => {
  test.setTimeout(SOLVER_TIMEOUT_MS);
  const loaded: string[] = [];
  context.on('response', (response) => {
    const path = new URL(response.url()).pathname;
    if (response.ok() && /\.(wasm|gz)$/.test(path)) {
      loaded.push(path.endsWith('.wasm') ? 'wasm' : 'strategy');
    }
  });
  await page.goto(FAST_URL);
  await byId(page, 'opponent-cfr').click();
  await expect(byId(page, 'opponent-cfr')).toHaveAttribute('aria-pressed', 'true');
  await byId(page, 'start-cash').click();
  let hands = 0;
  while (hands < 3) {
    const waypoint = await waitForWaypoint(page);
    if (waypoint === 'next-hand') {
      hands += 1;
      await byId(page, 'next-hand').click();
    } else {
      await byId(page, 'passive').click();
    }
  }
  expect(loaded.sort()).toEqual(['strategy', 'wasm']);
  const bots = await page.evaluate(
    () =>
      new Promise<string[]>((resolve, reject) => {
        const open = indexedDB.open('mallorca');
        open.onerror = () => reject(open.error);
        open.onsuccess = () => {
          const all = open.result.transaction('hands').objectStore('hands').getAll();
          all.onerror = () => reject(all.error);
          all.onsuccess = () => resolve(all.result.map((r: { bot: { id: string } }) => r.bot.id));
        };
      }),
  );
  expect(bots.length).toBeGreaterThanOrEqual(3);
  expect(new Set(bots)).toEqual(new Set(['cfr']));
});
