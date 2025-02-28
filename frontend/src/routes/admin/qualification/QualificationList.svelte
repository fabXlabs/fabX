<script lang="ts">
	import type { Qualification } from '$lib/api/model/qualification';

	interface Props {
		qualifications: Qualification[]
	}
	let { qualifications }: Props = $props();
</script>

<table>
	<thead>
	<tr>
		<th>Name</th>
		<th>Description</th>
	</tr>
	</thead>
	<tbody>
	{#each qualifications as qualification}
		<tr>
			<td>{qualification.name}</td>
			<td>{qualification.description}</td>
		</tr>
	{/each}
	</tbody>
</table>
