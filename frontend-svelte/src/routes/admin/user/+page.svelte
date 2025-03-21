<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { AugmentedUser } from '$lib/api/model/user';
	import { goto } from '$app/navigation';
	import type { SortingState } from '@tanstack/table-core';
	import { UserRoundPlus } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';

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
		goto(`/admin/user/${data.id}`);
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
		<!-- TODO implement adding user -->
		<Button class="normal-case" onclick={() => { alert("NOT YET IMPLEMENTED") }}>
			<UserRoundPlus />
			Add User
		</Button>
	{/snippet}
</DataTable>
