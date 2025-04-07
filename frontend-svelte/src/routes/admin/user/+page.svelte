<script lang="ts">
  import type { PageProps } from './$types';
  import DataTable from '$lib/components/ui/DataTable.svelte';
  import { columns } from './columns';
  import type { AugmentedUser } from '$lib/api/model/user';
  import { goto } from '$app/navigation';
  import type { SortingState } from '@tanstack/table-core';
  import { base } from '$app/paths';
  import AddUserSheet from './AddUserSheet.svelte';

  let { data }: PageProps = $props();

	let initialColumnVisibility = {
		'wikiName': false,
		'locked': false,
		'notes': false,
		'instructorQualifications': false
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

	function rowClick(data: AugmentedUser) {
		goto(`${base}/admin/user/${data.id}`);
	}
</script>

<DataTable
	{columns}
	data={data.augmentedUsers}
	{initialColumnVisibility}
	{initialSortingState}
	onRowSelect={rowClick}
>
	{#snippet addButton()}
    <AddUserSheet />
	{/snippet}
</DataTable>
