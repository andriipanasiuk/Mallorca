import {
  byId,
  expect,
  FAST_URL,
  playToTheEnd,
  stackOf,
  test,
  TOTAL_CHIPS,
  waitForWaypoint,
} from './fixtures.ts';

const ALL_IN_TIMEOUT_MS = 600_000;

test('a passive hand ends with the chips conserved', async ({ page }) => {
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  for (;;) {
    const waypoint = await waitForWaypoint(page);
    if (waypoint === 'next-hand') {
      break;
    }
    expect(waypoint).toBe('act');
    await byId(page, 'passive').click();
  }
  await expect(byId(page, 'result')).toBeVisible();
  expect((await stackOf(page, 0)) + (await stackOf(page, 1))).toBe(TOTAL_CHIPS);
  const replies = await page.evaluate(() => window.__workerReplies);
  const created = await page.evaluate(() => window.__workerCreated);
  expect(created).toBeGreaterThan(0);
  expect(replies).toBeGreaterThan(0);
});

test('all in on every hand ends the tournament and play again returns to a fresh table', async ({
  page,
}) => {
  test.setTimeout(ALL_IN_TIMEOUT_MS);
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  for (;;) {
    const waypoint = await waitForWaypoint(page);
    if (waypoint === 'tournament-over') {
      break;
    }
    if (waypoint === 'next-hand') {
      await byId(page, 'next-hand').click();
      continue;
    }
    if (await byId(page, 'raise').isEnabled()) {
      await byId(page, 'preset-allin').click();
      await byId(page, 'raise').click();
    } else {
      await byId(page, 'passive').click();
    }
  }
  await expect(byId(page, 'tournament-over')).toBeVisible();
  const total = (await stackOf(page, 0)) + (await stackOf(page, 1));
  expect(total).toBe(TOTAL_CHIPS);
  expect(Math.min(await stackOf(page, 0), await stackOf(page, 1))).toBe(0);
  await byId(page, 'play-again').click();
  await expect(byId(page, 'tournament-over')).toHaveCount(0);
  await expect(byId(page, 'start')).toBeVisible();
  await byId(page, 'start').click();
  await expect(byId(page, 'hand-number')).toContainText('1');
  for (const seat of [0, 1] as const) {
    const stack = await stackOf(page, seat);
    expect(stack).toBeGreaterThan(1900);
    expect(stack).toBeLessThanOrEqual(2000);
  }
});

test('leaving for tonight returns to a farewell start screen', async ({ page }) => {
  test.setTimeout(ALL_IN_TIMEOUT_MS);
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  await playToTheEnd(page);
  await byId(page, 'leave').click();
  await expect(byId(page, 'tournament-over')).toHaveCount(0);
  await expect(byId(page, 'farewell')).toHaveText('Sleep well. The table will be here.');
  await byId(page, 'start').click();
  await expect(byId(page, 'hand-number')).toContainText('1');
  await page.reload();
  await expect(byId(page, 'start')).toBeVisible();
  await expect(byId(page, 'farewell')).toHaveCount(0);
});

test('the next hand starts by itself after a fold', async ({ page }) => {
  await page.goto('/?seed=42&botDelay=0');
  await byId(page, 'start').click();
  await expect(byId(page, 'fold')).toBeEnabled();
  await byId(page, 'fold').click();
  await expect(byId(page, 'next-hand')).toBeVisible();
  await expect(byId(page, 'hand-number')).toHaveText('Hand 2', { timeout: 3000 });
  await expect(byId(page, 'next-hand')).toHaveCount(0);
});

test('reloading in the middle of a hand shows the start screen', async ({ page }) => {
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await page.reload();
  await expect(byId(page, 'start')).toBeVisible();
  await expect(byId(page, 'passive')).toHaveCount(0);
});
