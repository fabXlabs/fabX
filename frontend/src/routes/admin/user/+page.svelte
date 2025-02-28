<script lang="ts">
	import type { PageProps } from './$types';
	import AdminEntityList from '$lib/components/AdminEntityList.svelte';
	import UserList from './UserList.svelte';

	let { data }: PageProps = $props();
</script>

<AdminEntityList name="Users">
	<UserList users={data.users} />
</AdminEntityList>
