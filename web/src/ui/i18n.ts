import i18next from 'i18next';
import en from '../locales/en.json';

void i18next.init({
  lng: 'en',
  fallbackLng: 'en',
  resources: { en: { translation: en } },
  interpolation: { escapeValue: false },
  initAsync: false,
});

export const t = (key: string, options?: Record<string, string | number>): string =>
  i18next.t(key, options);
