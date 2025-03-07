<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import { goto } from '$app/navigation';
	import type { SortingState } from '@tanstack/table-core';
	import type { AugmentedTool } from '$lib/api/model/tool';

	let { data }: PageProps = $props();

	let initialColumnVisibility = {
		'requires2FA': false,
		'time': false,
		'idleState': false,
		'notes': false,
		'wikiLink': false
	};

	let initialSortingState: SortingState = [
		{
			id: 'name',
			desc: false
		}
	];

	function rowClick(data: AugmentedTool) {
		goto(`/admin/tool/${data.id}`);
	}
</script>

<DataTable
	{columns}
	data={data.augmentedTools}
	{initialColumnVisibility}
	{initialSortingState}
	onRowSelect={rowClick}
/>
