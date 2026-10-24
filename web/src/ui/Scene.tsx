import type { ComponentChildren } from 'preact';
import { geometryStyle, LANDSCAPE, PORTRAIT, SIMPLE_LANDSCAPE, SIMPLE_PORTRAIT } from './scene.ts';
import landscapeBlur from './scene/attic-landscape-blur.webp';
import landscape from './scene/attic-landscape.webp';
import portraitBlur from './scene/attic-portrait-blur.webp';
import portrait from './scene/attic-portrait.webp';

export interface SceneProps {
  children: ComponentChildren;
  dimmed?: boolean;
  simple?: boolean;
}

const ROOM_STYLE = {
  ...geometryStyle(LANDSCAPE, 'l'),
  ...geometryStyle(PORTRAIT, 'p'),
  '--l-blur': `url(${landscapeBlur})`,
  '--p-blur': `url(${portraitBlur})`,
};

const SIMPLE_STYLE = {
  ...geometryStyle(SIMPLE_LANDSCAPE, 'l'),
  ...geometryStyle(SIMPLE_PORTRAIT, 'p'),
};

export function Scene({ children, dimmed = false, simple = false }: SceneProps) {
  const className = ['scene', dimmed ? 'scene-dimmed' : '', simple ? 'scene-simple' : '']
    .filter(Boolean)
    .join(' ');
  return (
    <div class={className} data-testid="scene" style={simple ? SIMPLE_STYLE : ROOM_STYLE}>
      {!simple && (
        <picture class="scene-plate">
          <source media="(min-aspect-ratio: 1/1)" srcset={landscape} />
          <img src={portrait} alt="" decoding="async" />
        </picture>
      )}
      <div class="scene-vignette" />
      <div class="scene-content">{children}</div>
    </div>
  );
}
