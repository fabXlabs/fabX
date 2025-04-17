import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

const baseDir = process.env.BASE_DIR || '';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	// Consult https://svelte.dev/docs/kit/integrations
	// for more information about preprocessors
	preprocess: vitePreprocess(),

	kit: {
		paths: {
            base: baseDir
		},
		adapter: adapter({
			pages: 'target/fabx-svelte/',
			assets: 'target/fabx-svelte/',
			fallback: 'index.html',
		})
	}
};

export default config;
