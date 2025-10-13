<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { AugmentedUser } from '$lib/api/model/user';
	import ChangeIsAdminAlert from './ChangeIsAdminAlert.svelte';
	import DeleteUserAlert from './DeleteUserAlert.svelte';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<Card.Title class="text-lg">Danger Zone</Card.Title>
	</Card.Header>
	<Card.Content class="flex flex-col gap-2">
		<ChangeIsAdminAlert {user} />
		<DeleteUserAlert {user} />
	</Card.Content>
</Card.Root>
