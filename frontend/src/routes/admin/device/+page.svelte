<script lang="ts">
	import type { PageProps } from './$types';
	import AdminEntityList from '$lib/components/AdminEntityList.svelte';
	import DeviceList from './DeviceList.svelte';

	let { data }: PageProps = $props();
</script>

<AdminEntityList name="Devices">
	<DeviceList devices={data.devices} />
</AdminEntityList>
