<script lang="ts">
	import SiteHeader from "$lib/components/SiteHeader.svelte";

	let { children } = $props();
</script>

<SiteHeader />
<div class="flex-1">
	{@render children()}
</div>
