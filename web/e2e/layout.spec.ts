import { byId, expect, FAST_URL, test } from './fixtures.ts';

const CONTROLS = ['fold', 'passive', 'raise'];

test('the start screen and the table fit the viewport without horizontal scroll', async ({
  page,
}) => {
  const viewport = page.viewportSize();
  if (viewport === null) {
    throw new Error('viewport is not set');
  }
  const scrollWidth = () => page.evaluate(() => document.documentElement.scrollWidth);
  await page.goto(FAST_URL);
  expect(await scrollWidth()).toBeLessThanOrEqual(viewport.width);
  await byId(page, 'start-tournament').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  expect(await scrollWidth()).toBeLessThanOrEqual(viewport.width);
  for (const id of CONTROLS) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
  for (const id of ['preset-half', 'preset-pot', 'preset-allin', 'raise-amount']) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
});

test('the cash table and the session summary fit the viewport', async ({ page }) => {
  const viewport = page.viewportSize();
  if (viewport === null) {
    throw new Error('viewport is not set');
  }
  const scrollWidth = () => page.evaluate(() => document.documentElement.scrollWidth);
  await page.goto(FAST_URL);
  await byId(page, 'start-cash').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  expect(await scrollWidth()).toBeLessThanOrEqual(viewport.width);
  for (const id of [...CONTROLS, 'session-result', 'leave']) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
  await byId(page, 'fold').click();
  await expect(byId(page, 'leave')).toBeEnabled();
  await byId(page, 'leave').click();
  await expect(byId(page, 'session-over')).toBeInViewport({ ratio: 1 });
  expect(await scrollWidth()).toBeLessThanOrEqual(viewport.width);
});
