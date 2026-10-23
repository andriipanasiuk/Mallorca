import { t } from './i18n.ts';

const CHIPS = ['chip-terracotta', 'chip-teal', 'chip-honey'];

export function Pot({ amount }: { amount: number }) {
  return (
    <div class="pot" data-testid="pot" data-amount={amount}>
      <span class="chip-stack" aria-hidden="true">
        {CHIPS.map((chip) => (
          <span key={chip} class={`chip ${chip}`} />
        ))}
      </span>
      {t('pot.amount', { amount })}
    </div>
  );
}
