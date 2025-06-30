<script lang="ts">
	import type { PageProps } from './$types';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.augmentedTool}
		<Crumbs tool={data.augmentedTool} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.augmentedTool.name}
		</h1>
		<h2>Required Qualifications</h2>
		<div>
			{#each data.augmentedTool.requiredQualifications as qualification (qualification.id)}
				<QualificationTag {qualification} />
			{/each}
		</div>
	{/if}
</div>
