<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { User } from '$lib/api/model/user';
	import { goto } from '$app/navigation';
	import type { SortingState } from '@tanstack/table-core';

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

	let { data }: PageProps = $props();

	function rowClick(data: User) {
		goto(`/admin/user/${data.id}`);
	}
</script>

<DataTable
	{columns}
	data={data.users}
	{initialColumnVisibility}
	{initialSortingState}
	onRowSelect={rowClick}
/>
