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

test('the cash status bar keeps long values inside the viewport', async ({ page }) => {
  const viewport = page.viewportSize();
  if (viewport === null) {
    throw new Error('viewport is not set');
  }
  await page.goto(FAST_URL);
  await byId(page, 'start-cash').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await page.evaluate(() => {
    const set = (id: string, text: string) => {
      const node = document.querySelector(`[data-testid="${id}"]`);
      if (node !== null) {
        node.textContent = text;
      }
    };
    set('session-result', '-123456789012.5 bb');
    set('hand-number', 'Hand 1000');
    set('blinds', 'Blinds 10/20');
  });
  const bar = page.locator('.status-bar');
  const overflow = await bar.evaluate((node) => node.scrollWidth - node.clientWidth);
  expect(overflow).toBeLessThanOrEqual(0);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(
    viewport.width,
  );
  for (const id of ['session-result', 'hand-number', 'leave']) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
  await page.locator('summary.menu-summary').click();
  await expect(byId(page, 'clear-history')).toBeInViewport({ ratio: 1 });
});

test('the cash status bar stays on one line when the text runs wider', async ({ page }) => {
  await page.goto(FAST_URL);
  await byId(page, 'start-cash').click();
  await expect(byId(page, 'passive')).toBeEnabled();
  await page.addStyleTag({ content: '.status-bar { letter-spacing: 0.12em; }' });
  const leave = await byId(page, 'leave').boundingBox();
  const bar = await page.locator('.status-bar').boundingBox();
  if (leave === null || bar === null) {
    throw new Error('status bar is not rendered');
  }
  expect(bar.height).toBeLessThan(leave.height * 2);
  for (const id of [...CONTROLS, 'session-result', 'leave']) {
    await expect(byId(page, id)).toBeInViewport({ ratio: 1 });
  }
});
