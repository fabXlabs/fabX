<script lang="ts">
	import type { Tool } from '$lib/api/model/tool';

	interface Props {
		attachedTools: Record<number, Tool>;
	}

	let { attachedTools }: Props = $props();
</script>

{#each Object.entries(attachedTools) as [pin, tool] }
	<div>{pin}: {tool.name}</div>
{/each}
