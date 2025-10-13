<script lang="ts">
	import { asset } from '$app/paths';
	import { ModeWatcher } from 'mode-watcher';
	import { Toaster } from '$lib/components/ui/sonner/index.js';
	import { PUBLIC_FABX_VERSION } from '$env/static/public';
	import '../app.css';

	let { children } = $props();

	/* eslint-disable  @typescript-eslint/no-explicit-any */
	function onerror(e: any) {
		console.error('global error boundary 1', e);
		console.error('global error boundary 2', JSON.stringify(e));
	}
</script>

<svelte:head>
	<title>fabX</title>
	<link
		rel="preload"
		as="font"
		href={asset('/fonts/routed-gothic.ttf')}
		type="font/ttf"
		crossorigin="anonymous"
	/>
	<link
		rel="preload"
		as="font"
		href={asset('/fonts/routed-gothic-half-italic.ttf')}
		type="font/ttf"
		crossorigin="anonymous"
	/>
</svelte:head>

<ModeWatcher />
<Toaster richColors closeButton position="top-right" />
<div class="bg-background relative flex min-h-screen flex-col" id="page">
	<svelte:boundary {onerror}>{@render children()}</svelte:boundary>
</div>
<div class="bg-background text-foreground/60 m-5 text-center font-mono text-xs">
	fabX v{PUBLIC_FABX_VERSION}
</div>
