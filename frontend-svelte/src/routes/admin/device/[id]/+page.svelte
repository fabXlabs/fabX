<script lang="ts">
	import type { PageProps } from './$types';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.augmentedDevice}
		<Crumbs device={data.augmentedDevice} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.augmentedDevice.name}
		</h1>
	{/if}
</div>
