import preact from '@preact/preset-vite';
import { defineConfig } from 'vite';

export default defineConfig({
  base: process.env.BASE_PATH ?? '/',
  plugins: [preact()],
});
