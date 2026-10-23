import { byId, expect, FAST_URL, stackOf, test, TOTAL_CHIPS, waitForWaypoint } from './fixtures.ts';

const ALL_IN_TIMEOUT_MS = 600_000;

test('a passive hand ends with the chips conserved', async ({ page }) => {
  await page.goto(FAST_URL);
  await byId(page, 'start-tournament').click();
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
  await byId(page, 'start-tournament').click();
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
  await expect(byId(page, 'start-tournament')).toBeVisible();
  await byId(page, 'start-tournament').click();
  await expect(byId(page, 'hand-number')).toContainText('1');
  for (const seat of [0, 1] as const) {
    const stack = await stackOf(page, seat);
    expect(stack).toBeGreaterThan(1900);
    expect(stack).toBeLessThanOrEqual(2000);
  }
});

test('reloading in the middle of a hand shows the start screen', async ({ page }) => {
  await page.goto(FAST_URL);
  await byId(page, 'start-tournament').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await page.reload();
  await expect(byId(page, 'start-tournament')).toBeVisible();
  await expect(byId(page, 'passive')).toHaveCount(0);
});

test('a cash game is played for a hand, left, and the session summary shows one hand', async ({
  page,
}) => {
  await page.goto('/?seed=3&botDelay=0');
  await byId(page, 'start-cash').click();
  await expect(byId(page, 'session-result')).toBeVisible();
  await expect(byId(page, 'leave')).toBeDisabled();
  for (;;) {
    const waypoint = await waitForWaypoint(page);
    if (waypoint === 'next-hand') {
      break;
    }
    expect(waypoint).toBe('act');
    await byId(page, 'passive').click();
  }
  await expect(byId(page, 'leave')).toBeEnabled();
  await byId(page, 'leave').click();
  await expect(byId(page, 'session-over')).toBeVisible();
  await expect(byId(page, 'session-summary')).toContainText('over 1 hands');
  await byId(page, 'play-again').click();
  await expect(byId(page, 'start-cash')).toBeVisible();
});
