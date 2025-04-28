<script lang="ts">
	import { base } from '$app/paths';
	import { ModeWatcher } from 'mode-watcher';
	import '../app.css';

	let { children } = $props();

	function onerror(e: any) {
		console.error('global error boundary', e);
	}
</script>

<svelte:head>
	<title>fabX</title>
	<link
		rel="preload"
		as="font"
		href="{base}/fonts/routed-gothic.ttf"
		type="font/ttf"
		crossorigin="anonymous"
	/>
	<link
		rel="preload"
		as="font"
		href="{base}/fonts/routed-gothic-half-italic.ttf"
		type="font/ttf"
		crossorigin="anonymous"
	/>
</svelte:head>

<ModeWatcher />
<div class="bg-background relative flex min-h-screen flex-col" id="page">
	<svelte:boundary {onerror}>{@render children()}</svelte:boundary>
</div>
<!-- TODO show version -->
<div class="bg-background text-foreground/60 m-5 text-center font-mono text-xs">fabX vTODO</div>
