<script lang="ts">
	import type { Tool } from '$lib/api/model/tool';

	interface Props {
		tools: Tool[];
	}

	let { tools }: Props = $props();
</script>


<table>
	<thead>
	<tr>
		<th>Name</th>
		<th>Type</th>
		<th>Enabled</th>
		<th>2FA</th>
		<th>Qualifications</th>
	</tr>
	</thead>
	<tbody>
	{#each tools as tool}
		<tr>
			<td>{tool.name}</td>
			<td>{tool.type}</td>
			<td>{tool.enabled}</td>
			<td>{tool.requires2FA}</td>
			<td>{tool.requiredQualifications}</td>
		</tr>
	{/each}
	</tbody>
</table>
