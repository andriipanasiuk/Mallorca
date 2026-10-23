import { t } from './i18n.ts';

export function Pot({ amount }: { amount: number }) {
  return (
    <div class="pot" data-testid="pot" data-amount={amount}>
      {t('pot.amount', { amount })}
    </div>
  );
}
