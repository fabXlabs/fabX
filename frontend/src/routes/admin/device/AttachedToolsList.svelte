<script lang="ts">
	import type { Tool } from '$lib/api/model/tool';

	interface Props {
		attachedTools: Record<number, Tool>;
	}

	let { attachedTools }: Props = $props();
</script>

<table class="border-spacing-1">
	<tbody>
		{#each Object.entries(attachedTools) as [pin, tool] (pin)}
			<tr>
				<td class="px-1 text-right">{pin}: </td>
				<td>{tool.name}</td>
			</tr>
		{/each}
	</tbody>
</table>
