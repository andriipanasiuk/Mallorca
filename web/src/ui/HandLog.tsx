import { useEffect, useRef } from 'preact/hooks';
import { t } from './i18n.ts';

export function HandLog({ lines }: { lines: string[] }) {
  const list = useRef<HTMLOListElement>(null);
  useEffect(() => {
    const node = list.current;
    if (node !== null) {
      node.scrollTop = node.scrollHeight;
    }
  }, [lines.length]);
  return (
    <aside class="hand-log" data-testid="hand-log" aria-label={t('log.title')}>
      <h2 class="hand-log-title">{t('log.title')}</h2>
      <ol class="hand-log-lines" ref={list}>
        {lines.map((line, index) => (
          <li key={index}>{line}</li>
        ))}
      </ol>
    </aside>
  );
}
