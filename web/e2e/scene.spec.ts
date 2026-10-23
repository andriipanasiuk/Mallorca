import type { Page } from '@playwright/test';
import { byId, expect, FAST_URL, test, waitForWaypoint } from './fixtures.ts';

const ABORTED_PLATE = 'console.error: Failed to load resource: net::ERR_FAILED';

test('the table stays playable when the plate fails to load', async ({ page, problems }) => {
  let aborted = 0;
  await page.route('**/*.webp', (route) => {
    aborted += 1;
    return route.abort();
  });
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await expect(byId(page, 'passive')).toBeInViewport({ ratio: 1 });
  const felt = await byId(page, 'scene').evaluate((node) => getComputedStyle(node).backgroundColor);
  expect(felt).toBe('rgb(31, 74, 82)');
  const plateErrors = problems.filter((problem) => problem === ABORTED_PLATE).length;
  expect(plateErrors).toBeLessThanOrEqual(aborted);
  problems.splice(0, problems.length, ...problems.filter((problem) => problem !== ABORTED_PLATE));
});

async function playToTheFlop(page: Page) {
  const boardCard = page.locator('[data-testid="board"] [data-testid="card"]').first();
  for (let step = 0; step < 30 && (await boardCard.count()) === 0; step += 1) {
    const waypoint = await waitForWaypoint(page);
    if (waypoint === 'next-hand') {
      await byId(page, 'next-hand').click();
    } else if (waypoint === 'act') {
      await byId(page, 'passive').click();
    }
  }
  await expect(boardCard).toBeVisible();
  return boardCard;
}

test('reduced motion turns off the deal animation', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  const boardCard = await playToTheFlop(page);
  expect(await boardCard.evaluate((node) => node.getAnimations().length)).toBe(0);
});

test('without reduced motion a dealt card plays the deal animation', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'no-preference' });
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  const boardCard = await playToTheFlop(page);
  const names = await boardCard.evaluate((node) =>
    node.getAnimations().map((animation) => (animation as CSSAnimation).animationName),
  );
  expect(names).toContain('deal-in');
});
