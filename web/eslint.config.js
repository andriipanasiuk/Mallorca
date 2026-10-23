import js from '@eslint/js';
import tseslint from 'typescript-eslint';

const pure = ['src/cards', 'src/equity', 'src/engine', 'src/brains'];

const layer = (dir, allowed) => ({
  files: [`src/${dir}/**/*.ts`, `src/${dir}/**/*.tsx`],
  rules: {
    'no-restricted-imports': [
      'error',
      {
        patterns: [
          {
            regex: `(^|/)(${['cards', 'equity', 'engine', 'brains', 'game', 'ui']
              .filter((l) => l !== dir && !allowed.includes(l))
              .join('|')})(/|$)`,
            message: `src/${dir} may import only from ${allowed.length ? allowed.join(', ') : 'itself'}`,
          },
        ],
      },
    ],
  },
});

export default tseslint.config(
  {
    ignores: ['**/dist', 'node_modules', 'test-results', 'playwright-report'],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.ts', '**/*.tsx'],
    rules: {
      '@typescript-eslint/consistent-type-imports': 'error',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    },
  },
  layer('cards', []),
  layer('equity', ['cards']),
  layer('engine', ['cards', 'equity']),
  layer('brains', ['cards', 'equity', 'engine']),
  layer('game', ['cards', 'equity', 'engine', 'brains']),
  layer('ui', ['game']),
  ...pure.map((dir) => ({
    files: [`${dir}/**/*.ts`],
    rules: {
      'no-restricted-globals': [
        'error',
        { name: 'Date', message: 'Pass time in as an argument' },
        { name: 'performance', message: 'Pass time in as an argument' },
        { name: 'document', message: 'No DOM in this layer' },
        { name: 'window', message: 'No DOM in this layer' },
      ],
      'no-restricted-properties': [
        'error',
        { object: 'Math', property: 'random', message: 'Randomness enters as an Rng' },
      ],
    },
  })),
);
