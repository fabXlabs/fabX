import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';
import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { enhancedImages } from '@sveltejs/enhanced-img';
import { defineConfig } from 'vite';

const baseDir = process.env.BASE_DIR || '';

export default defineConfig({
	plugins: [
		tailwindcss(),
		enhancedImages() /* must come before the SvelteKit plugin */,
		sveltekit({
			// Consult https://svelte.dev/docs/kit/integrations
			// for more information about preprocessors
			preprocess: vitePreprocess(),
			paths: { base: baseDir },
			adapter: adapter({
				pages: 'target/fabx-svelte/',
				assets: 'target/fabx-svelte/',
				fallback: 'index.html'
			})
		})
	],
	server: { proxy: { '/api': 'http://localhost:8080' } }
});
