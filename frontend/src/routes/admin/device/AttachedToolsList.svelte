<script lang="ts">
	import type { Tool } from '$lib/api/model/tool';

	interface Props {
		attachedTools: Record<number, Tool>;
	}

	let { attachedTools }: Props = $props();
</script>

<!-- TODO make table (like AttachedInputsList) -->
{#each Object.entries(attachedTools) as [pin, tool] (pin)}
	<div>{pin}: {tool.name}</div>
{/each}
