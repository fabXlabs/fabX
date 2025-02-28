<script lang="ts">
	import type { PageProps } from './$types';
	import AdminEntityList from '$lib/components/AdminEntityList.svelte';

	let { data }: PageProps = $props();
</script>

<AdminEntityList name="Devices">
	<div>TODO</div>
</AdminEntityList>
