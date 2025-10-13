<script lang="ts">
	import { Toaster as Sonner, type ToasterProps as SonnerProps } from 'svelte-sonner';
	import { mode } from 'mode-watcher';

	let { ...restProps }: SonnerProps = $props();
</script>

<Sonner theme={mode.current} class="toaster group" {...restProps} />
