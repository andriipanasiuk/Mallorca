import { cleanup, render } from '@testing-library/preact';
import { afterEach, describe, expect, it } from 'vitest';
import { Scene } from './Scene.tsx';

afterEach(cleanup);

const FIELDS = [
  'focus-x',
  'focus-y',
  'felt-tilt',
  'opponent-y',
  'board-y',
  'hero-bottom',
  'card-scale',
];

describe('Scene', () => {
  it('renders the landscape source for wide screens and the portrait image', () => {
    const { container } = render(
      <Scene>
        <p>x</p>
      </Scene>,
    );
    const source = container.querySelector('picture source');
    expect(source?.getAttribute('media')).toBe('(min-aspect-ratio: 1/1)');
    expect(source?.getAttribute('srcset')).toContain('attic-landscape');
    const image = container.querySelector('picture img');
    expect(image?.getAttribute('src')).toContain('attic-portrait');
    expect(image?.getAttribute('alt')).toBe('');
  });

  it('exposes every geometry field for both plates as custom properties', () => {
    const { getByTestId } = render(
      <Scene>
        <p>x</p>
      </Scene>,
    );
    const style = getByTestId('scene').getAttribute('style') ?? '';
    for (const prefix of ['l', 'p']) {
      for (const name of FIELDS) {
        expect(style).toContain(`--${prefix}-${name}`);
      }
    }
  });

  it('dims the scene behind the start screen', () => {
    const { getByTestId } = render(
      <Scene dimmed>
        <p>x</p>
      </Scene>,
    );
    expect(getByTestId('scene').classList.contains('scene-dimmed')).toBe(true);
  });

  it('draws the simple table without loading the photo', () => {
    const { container, getByTestId } = render(
      <Scene simple>
        <p>x</p>
      </Scene>,
    );
    expect(container.querySelector('picture')).toBeNull();
    expect(getByTestId('scene').classList.contains('scene-simple')).toBe(true);
    expect(getByTestId('scene').getAttribute('style') ?? '').not.toContain('blur.webp');
  });

  it('lays the simple table flat', () => {
    const { getByTestId } = render(
      <Scene simple>
        <p>x</p>
      </Scene>,
    );
    const style = getByTestId('scene').getAttribute('style') ?? '';
    expect(style).toContain('--l-felt-tilt: 0deg');
    expect(style).toContain('--p-felt-tilt: 0deg');
  });

  it('keeps its children inside the scene content', () => {
    const { getByTestId } = render(
      <Scene>
        <p data-testid="child">x</p>
      </Scene>,
    );
    expect(
      getByTestId('scene').querySelector('.scene-content [data-testid="child"]'),
    ).not.toBeNull();
  });
});
