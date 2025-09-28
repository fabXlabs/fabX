<script lang="ts">
	import type { PageProps } from './$types';
	import Crumbs from './Crumbs.svelte';
	import DeviceDetailsCard from './DeviceDetailsCard.svelte';
	import DeviceFirmwareCard from './DeviceFirmwareCard.svelte';
	import AttachedToolsCard from './AttachedToolsCard.svelte';
	import DangerZoneCard from './DangerZoneCard.svelte';

	let { data }: PageProps = $props();
</script>

<!-- TODO: Show Image -->
<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.augmentedDevice}
		<Crumbs device={data.augmentedDevice} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.augmentedDevice.name}
		</h1>
		<div class="my-6 grid gap-4">
			<DeviceDetailsCard device={data.augmentedDevice} />
			<DeviceFirmwareCard device={data.augmentedDevice} />
			<AttachedToolsCard device={data.augmentedDevice} tools={data.tools} />
			<DangerZoneCard device={data.augmentedDevice} />
		</div>
	{/if}
</div>
