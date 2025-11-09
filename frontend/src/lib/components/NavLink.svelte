<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { cn } from '$lib/utils';
	import { page } from '$app/state';

	let { sheetOpen = $bindable(false), href, children, classes = '', ...others } = $props();

	function closeSheet() {
		sheetOpen = false;
	}
</script>

<a
	class={cn(
		'hover:text-foreground/80 text-foreground/60 font-accent transition-colors',
		classes,
		page.url.pathname.startsWith(href) ? 'text-foreground' : 'text-foreground/60'
	)}
	onclick={closeSheet}
	{href}
	{...others}>{@render children()}</a
>
