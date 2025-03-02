<script lang="ts">
	import type { User } from '$lib/api/model/user';

	interface Props {
		users: User[];
	}

	let { users }: Props = $props();
</script>

<table>
	<thead>
	<tr>
		<th>Name</th>
		<th>Type</th>
	</tr>
	</thead>
	<tbody>
	{#each users as user}
		<tr>
			<td>{user.firstName} {user.lastName}</td>
			<td>{user.memberQualifications}</td>
		</tr>
	{/each}
	</tbody>
</table>
