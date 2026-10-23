import { byId, expect, FAST_URL, test } from './fixtures.ts';

test.fixme('the table stays playable when the plate fails to load', async ({ page }) => {
  await page.route('**/*.webp', (route) => route.abort());
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await expect(byId(page, 'passive')).toBeInViewport({ ratio: 1 });
  const felt = await byId(page, 'scene').evaluate((node) => getComputedStyle(node).backgroundColor);
  expect(felt).toBe('rgb(31, 74, 82)');
});
