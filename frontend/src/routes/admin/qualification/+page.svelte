<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { SortingState } from '@tanstack/table-core';
	import { goto } from '$app/navigation';
	import type { Qualification } from '$lib/api/model/qualification';

	let { data }: PageProps = $props();

	let initialSortingState: SortingState = [
		{
			id: 'name',
			desc: false
		}
	];

	function rowClick(data: Qualification) {
		goto(`/admin/qualification/${data.id}`);
	}
</script>

<DataTable
	{columns}
	data={data.qualifications}
	initialColumnVisibility={{}}
	{initialSortingState}
	onRowSelect={rowClick}
/>
