<script lang="ts">
	import type { FabXError } from '$lib/api/model/error';

	interface Props {
		error: FabXError | null;
	}

	let { error }: Props = $props();
</script>

{#if error}
	<div class="*:break-all">
		<p class="text-sm text-red-600">Error: {error.message}</p>
		{#if error.type}
			<p class="text-xs text-red-800">{error.type}</p>
		{/if}
		{#if error.parameters}
			{#each Object.entries(error.parameters) as parameter}
				<p class="text-xs text-red-800">{parameter[0]}: {parameter[1]}</p>
			{/each}
		{/if}
		<p class="text-xs text-red-800">{error.correlationId || 'None'}</p>
	</div>
{/if}
