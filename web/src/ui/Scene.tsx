import type { ComponentChildren } from 'preact';
import { geometryStyle, LANDSCAPE, PORTRAIT } from './scene.ts';
import landscapeBlur from './scene/attic-landscape-blur.webp';
import landscape from './scene/attic-landscape.webp';
import portraitBlur from './scene/attic-portrait-blur.webp';
import portrait from './scene/attic-portrait.webp';

export interface SceneProps {
  children: ComponentChildren;
  dimmed?: boolean;
}

const STYLE = {
  ...geometryStyle(LANDSCAPE, 'l'),
  ...geometryStyle(PORTRAIT, 'p'),
  '--l-blur': `url(${landscapeBlur})`,
  '--p-blur': `url(${portraitBlur})`,
};

export function Scene({ children, dimmed = false }: SceneProps) {
  return (
    <div class={`scene${dimmed ? ' scene-dimmed' : ''}`} data-testid="scene" style={STYLE}>
      <picture class="scene-plate">
        <source media="(min-aspect-ratio: 1/1)" srcset={landscape} />
        <img src={portrait} alt="" decoding="async" />
      </picture>
      <div class="scene-vignette" />
      <div class="scene-content">{children}</div>
    </div>
  );
}
