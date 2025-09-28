<script lang="ts">
	import type { PageProps } from './$types';
	import Crumbs from './Crumbs.svelte';
	import ToolDetailsCard from './ToolDetailsCard.svelte';
	import DangerZoneCard from './DangerZoneCard.svelte';

	let { data }: PageProps = $props();
</script>

<!-- TODO: Show Image -->
<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.augmentedTool}
		<Crumbs tool={data.augmentedTool} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.augmentedTool.name}
		</h1>
		<div class="my-6 grid gap-4">
			<ToolDetailsCard tool={data.augmentedTool} qualifications={data.qualifications} />
			<DangerZoneCard tool={data.augmentedTool} />
		</div>
	{/if}
</div>
