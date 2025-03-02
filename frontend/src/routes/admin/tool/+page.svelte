<script lang="ts">
	import type { PageProps } from './$types';
	import AdminEntityList from '$lib/components/AdminEntityList.svelte';
	import ToolList from './ToolList.svelte';

	let { data }: PageProps = $props();
</script>

<AdminEntityList name="Tools">
	<ToolList tools={data.tools} />
</AdminEntityList>
