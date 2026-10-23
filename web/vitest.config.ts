import { configDefaults, defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    include: ['src/**/*.test.ts', 'tools/**/*.test.ts'],
    exclude: [...configDefaults.exclude, '**/*.golden.test.ts'],
  },
});
