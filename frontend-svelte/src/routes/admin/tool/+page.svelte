<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import { goto } from '$app/navigation';
	import type { SortingState } from '@tanstack/table-core';
	import type { AugmentedTool } from '$lib/api/model/tool';
	import { Button } from '$lib/components/ui/button';
	import { Plus } from 'lucide-svelte';
	import { base } from '$app/paths';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();

	let initialColumnVisibility = {
		requires2FA: false,
		time: false,
		idleState: false,
		notes: false,
		wikiLink: false
	};

	let initialSortingState: SortingState = [
		{
			id: 'name',
			desc: false
		}
	];

	function rowClick(data: AugmentedTool) {
		goto(`${base}/admin/tool/${data.id}`);
	}
</script>

<DataTable
	{columns}
	data={data.augmentedTools}
	{initialColumnVisibility}
	{initialSortingState}
	onRowSelect={rowClick}
>
	{#snippet breadCrumbs()}
		<Crumbs />
	{/snippet}
	{#snippet addButton()}
		<!-- TODO implement adding tool -->
		<Button
			class="normal-case"
			onclick={() => {
				alert('NOT YET IMPLEMENTED');
			}}
		>
			<Plus />
			Add Tool
		</Button>
	{/snippet}
</DataTable>
