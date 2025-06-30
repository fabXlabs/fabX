<script lang="ts">
	import { base } from '$app/paths';
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { SortingState } from '@tanstack/table-core';
	import { goto } from '$app/navigation';
	import type { Qualification } from '$lib/api/model/qualification';
	import { Button } from '$lib/components/ui/button';
	import { Plus } from 'lucide-svelte';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();

	let initialSortingState: SortingState = [
		{
			id: 'name',
			desc: false
		}
	];

	function rowClick(data: Qualification) {
		goto(`${base}/admin/qualification/${data.id}`);
	}
</script>

<DataTable
	{columns}
	data={data.qualifications}
	initialColumnVisibility={{}}
	{initialSortingState}
	onRowSelect={rowClick}
>
	{#snippet breadCrumbs()}
		<Crumbs />
	{/snippet}
	{#snippet addButton()}
		<!-- TODO implement adding qualification -->
		<Button
			class="normal-case"
			onclick={() => {
				alert('NOT YET IMPLEMENTED');
			}}
		>
			<Plus />
			Add Qualification
		</Button>
	{/snippet}
</DataTable>
