<script lang="ts">
	import SiteHeader from "$lib/components/site-header.svelte";

	let { children } = $props();
</script>

<SiteHeader />
<div class="flex-1">
	{@render children()}
</div>
