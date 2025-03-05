<script lang="ts">
	import type { PageProps } from './$types';
	import DataTable from '$lib/components/ui/DataTable.svelte';
	import { columns } from './columns';

	let initialColumnVisibility = {
		'wikiName': false,
		'locked': false,
		'notes': false,
		'instructorQualifications': false
	};

	let { data }: PageProps = $props();
</script>

<DataTable columns={columns} data={data.users} initialColumnVisibility={initialColumnVisibility} />
