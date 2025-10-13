<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { User } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	import { buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { invalidateAll } from '$app/navigation';
	import { hardDeleteUser } from '$lib/api/users.js';

	interface Props {
		user: User;
		open: boolean;
	}

	let { user, open = $bindable() }: Props = $props();

	let error: FabXError | null = $state(null);

	async function hardDeleteUser_() {
		error = null;
		const res = await hardDeleteUser(fetch, user.id).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await invalidateAll();
			open = false;
		}
	}
</script>

<AlertDialog.Root bind:open>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Hard-Delete {user.firstName}
				{user.lastName}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action hard-deletes the user {user.firstName}
				{user.lastName}. Hard-deletion cannot be undone.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={hardDeleteUser_}
				class={buttonVariants({ variant: 'destructive' })}
			>
				Continue
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
