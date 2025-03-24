<script lang="ts">
	import type { PageProps } from './$types';
	import QualificationTag from '$lib/components/QualificationTag.svelte';

	let { data }: PageProps = $props();
</script>

<div class="max-w-screen-2xl">
	<div class="container items-center py-4">
		{#if data.augmentedTool}
			<h1>Tool {data.augmentedTool.name}</h1>
			<h2>Required Qualifications</h2>
			<div>
				{#each data.augmentedTool.requiredQualifications as qualification}
					<QualificationTag {qualification} />
				{/each}
			</div>
		{/if}
	</div>
</div>
