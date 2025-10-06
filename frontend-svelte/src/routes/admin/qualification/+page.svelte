<script lang="ts">
	import { resolve } from '$app/paths';
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { SortingState } from '@tanstack/table-core';
	import { goto } from '$app/navigation';
	import type { Qualification } from '$lib/api/model/qualification';
	import Crumbs from './Crumbs.svelte';
	import AddQualificationSheet from './AddQualificationSheet.svelte';

	let { data }: PageProps = $props();

	let initialSortingState: SortingState = [
		{
			id: 'orderNr',
			desc: false
		}
	];

	function rowClick(data: Qualification) {
		goto(
			resolve('/admin/qualification/[id]', {
				id: data.id
			})
		);
	}
</script>

<DataTable
	{columns}
	data={data.qualifications}
	initialColumnVisibility={{ orderNr: false }}
	{initialSortingState}
	onRowSelect={rowClick}
	columnFilterSnippet={null}
>
	{#snippet breadCrumbs()}
		<Crumbs />
	{/snippet}
	{#snippet addButton()}
		<AddQualificationSheet />
	{/snippet}
</DataTable>
