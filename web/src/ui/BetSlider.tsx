import type { RaiseControl } from '../game/index.ts';
import { t } from './i18n.ts';

export interface BetSliderProps {
  raise: RaiseControl;
  value: number;
  disabled: boolean;
  onChange: (value: number) => void;
}

export function BetSlider({ raise, value, disabled, onChange }: BetSliderProps) {
  return (
    <div class="bet-slider">
      <input
        class="bet-range"
        type="range"
        data-testid="raise-amount"
        aria-label={t('actions.amount')}
        min={raise.min}
        max={raise.max}
        step={1}
        value={value}
        disabled={disabled}
        onInput={(event) => onChange(Number((event.currentTarget as HTMLInputElement).value))}
      />
      <div class="bet-presets">
        <button
          type="button"
          class="button button-small"
          data-testid="preset-half"
          disabled={disabled}
          onClick={() => onChange(raise.presets.halfPot)}
        >
          {t('actions.half')}
        </button>
        <button
          type="button"
          class="button button-small"
          data-testid="preset-pot"
          disabled={disabled}
          onClick={() => onChange(raise.presets.pot)}
        >
          {t('actions.pot')}
        </button>
        <button
          type="button"
          class="button button-small"
          data-testid="preset-allin"
          disabled={disabled}
          onClick={() => onChange(raise.presets.allIn)}
        >
          {t('actions.allIn')}
        </button>
      </div>
    </div>
  );
}
