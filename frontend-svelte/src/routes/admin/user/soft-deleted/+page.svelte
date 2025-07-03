<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { SortingState } from '@tanstack/table-core';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();

	let initialColumnVisibility = {};

	let initialSortingState: SortingState = [
		{
			id: 'firstName',
			desc: false
		}
	];
</script>

<DataTable {columns} data={data.users} {initialColumnVisibility} {initialSortingState}>
	{#snippet breadCrumbs()}
		<Crumbs />
	{/snippet}
</DataTable>
