<script lang="ts">
	import type { PageProps } from './$types';

	let { data }: PageProps = $props();
</script>

<div class="max-w-screen-2xl">
	<div class="container items-center py-4">
		{#if data.device}
			<h1>Device {data.device.name}</h1>
		{/if}
	</div>
</div>
