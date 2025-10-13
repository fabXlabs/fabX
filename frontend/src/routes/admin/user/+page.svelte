<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { AugmentedUser } from '$lib/api/model/user';
	import { goto } from '$app/navigation';
	import type { SortingState } from '@tanstack/table-core';
	import { resolve } from '$app/paths';
	import AddUserSheet from './AddUserSheet.svelte';
	import Crumbs from './Crumbs.svelte';
	import DataTableColumnFilter from '$lib/components/ui/DataTableColumnFilter.svelte';

	let { data }: PageProps = $props();

	let initialColumnVisibility = {
		wikiName: false,
		locked: false,
		notes: false,
		instructorQualifications: false
	};

	let initialSortingState: SortingState = [
		{
			id: 'isAdmin',
			desc: true
		},
		{
			id: 'firstName',
			desc: false
		}
	];

	let qualificationOptions = $derived.by(() => {
		return data.qualifications.map((q) => {
			return { label: q.name, value: q.id };
		});
	});

	function rowClick(data: AugmentedUser) {
		goto(
			resolve('/admin/user/[id]', {
				id: data.id
			})
		);
	}
</script>

<DataTable
	{columns}
	data={data.augmentedUsers}
	{initialColumnVisibility}
	{initialSortingState}
	onRowSelect={rowClick}
>
	{#snippet breadCrumbs()}
		<Crumbs />
	{/snippet}
	{#snippet columnFilterSnippet({ table })}
		<DataTableColumnFilter
			column={table.getColumn('memberQualifications')}
			title="Qualification"
			options={qualificationOptions}
		/>
		<DataTableColumnFilter
			column={table.getColumn('instructorQualifications')}
			title="Instructor"
			options={qualificationOptions}
		/>
	{/snippet}
	{#snippet addButton()}
		<AddUserSheet />
	{/snippet}
</DataTable>
