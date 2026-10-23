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

test('long amounts stay inside the small phone', async ({ page }) => {
  const viewport = page.viewportSize();
  if (viewport === null) {
    throw new Error('viewport is not set');
  }
  await page.goto(FAST_URL);
  await byId(page, 'start').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await byId(page, 'preset-allin').click();
  await expect(byId(page, 'raise')).toContainText('2000');
  for (const id of CONTROLS) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(
    viewport.width,
  );
  const overflowing = await page.evaluate(() =>
    ['fold', 'passive', 'raise'].filter((id) => {
      const node = document.querySelector(`[data-testid="${id}"]`);
      return node !== null && node.scrollWidth > node.clientWidth;
    }),
  );
  expect(overflowing).toEqual([]);
});

for (const viewport of [
  { width: 844, height: 390 },
  { width: 1280, height: 600 },
]) {
  test(`a short landscape screen keeps every control visible at ${viewport.width}x${viewport.height}`, async ({
    page,
  }) => {
    await page.setViewportSize(viewport);
    const scrollWidth = () => page.evaluate(() => document.documentElement.scrollWidth);
    await page.goto(FAST_URL);
    await byId(page, 'start').click();
    await expect(byId(page, 'passive')).toBeEnabled();
    expect(await scrollWidth()).toBeLessThanOrEqual(viewport.width);
    for (const id of [...CONTROLS, 'preset-half', 'preset-pot', 'preset-allin', 'raise-amount']) {
      await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
    }
  });
}
