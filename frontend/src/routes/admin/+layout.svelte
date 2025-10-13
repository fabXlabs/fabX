<script lang="ts">
	import SiteHeader from '$lib/components/SiteHeader.svelte';
	import type { LayoutProps } from './$types';
	import { setMeContext } from '$lib/context';

	let { data, children }: LayoutProps = $props();

	setMeContext(data.me);
</script>

<SiteHeader />
<div class="flex-1">
	{@render children()}
</div>
