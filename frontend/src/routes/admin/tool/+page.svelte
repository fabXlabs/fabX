<script lang="ts">
	import type { PageProps } from './$types';
	import AdminEntityList from '$lib/components/admin-entity-list.svelte';

	let { data }: PageProps = $props();
</script>

<AdminEntityList name="Tools">
	<div>TODO</div>
</AdminEntityList>
