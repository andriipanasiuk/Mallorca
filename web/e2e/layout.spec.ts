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
  await byId(page, 'start').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  expect(await scrollWidth()).toBeLessThanOrEqual(viewport.width);
  for (const id of CONTROLS) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
  for (const id of ['preset-half', 'preset-pot', 'preset-allin', 'raise-amount']) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
});
