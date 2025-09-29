<script lang="ts">
	import type { PageProps } from './$types';
	import Crumbs from './Crumbs.svelte';
	import ToolDetailsCard from './ToolDetailsCard.svelte';
	import DangerZoneCard from './DangerZoneCard.svelte';
	import ToolImage from '../ToolImage.svelte';
	import ChangeImageSheet from './ChangeImageSheet.svelte';

	let { data }: PageProps = $props();

	let changeImageSheetOpen = $state(false);
</script>

<ChangeImageSheet id={data.augmentedTool?.id || ''} bind:open={changeImageSheetOpen} />
<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.augmentedTool}
		<div class="flex justify-between">
			<div>
				<Crumbs tool={data.augmentedTool} />
				<h1 class="font-accent mt-4 mb-2 text-3xl">
					{data.augmentedTool.name}
				</h1>
			</div>
			<ToolImage
				id={data.augmentedTool.id}
				class="w-19"
				onclick={() => {
					changeImageSheetOpen = true;
				}}
			/>
		</div>
		<div class="my-6 grid gap-4">
			<ToolDetailsCard tool={data.augmentedTool} qualifications={data.qualifications} />
			<DangerZoneCard tool={data.augmentedTool} />
		</div>
	{/if}
</div>
