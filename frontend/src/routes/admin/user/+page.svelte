<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';
	import type { User } from '$lib/api/model/user';
	import { goto } from '$app/navigation';

	let initialColumnVisibility = {
		'wikiName': false,
		'locked': false,
		'notes': false,
		'instructorQualifications': false
	};

	let { data }: PageProps = $props();

	function rowClick(data: User) {
		goto(`/admin/user/${data.id}`);
	}
</script>

<DataTable
	columns={columns}
	data={data.users}
	initialColumnVisibility={initialColumnVisibility}
	onRowSelect={rowClick}
/>
