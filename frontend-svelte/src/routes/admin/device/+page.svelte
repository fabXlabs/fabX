<script lang="ts">
	import type { PageProps } from './$types';
	import { resolve } from '$app/paths';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import { goto } from '$app/navigation';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import type { SortingState } from '@tanstack/table-core';
	import Crumbs from './Crumbs.svelte';
	import AddDeviceSheet from './AddDeviceSheet.svelte';

	let { data }: PageProps = $props();

	let initialColumnVisibility = {
		background: false,
		backupBackendUrl: false,
		desiredFirmwareVersion: false
	};

	let initialSortingState: SortingState = [
		{
			id: 'name',
			desc: false
		}
	];

	function rowClick(data: AugmentedDevice) {
		goto(
			resolve(`/admin/device/[id]`, {
				id: data.id
			})
		);
	}
</script>

<!-- TODO: Show Images -->
<DataTable
	{columns}
	data={data.augmentedDevices}
	{initialColumnVisibility}
	{initialSortingState}
	onRowSelect={rowClick}
>
	{#snippet breadCrumbs()}
		<Crumbs />
	{/snippet}
	{#snippet addButton()}
		<AddDeviceSheet />
	{/snippet}
</DataTable>
