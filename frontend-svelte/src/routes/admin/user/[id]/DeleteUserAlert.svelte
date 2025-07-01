<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { AugmentedUser } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { goto } from '$app/navigation';
	import { deleteUser } from '$lib/api/users.js';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();

	let error: FabXError | null = $state(null);

	async function deleteUser_() {
		error = null;
		const res = await deleteUser(fetch, user.id).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await goto(`/admin/user/`);
		}
	}
</script>

<AlertDialog.Root>
	<AlertDialog.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline">Delete User</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Soft-Delete {user.firstName}
				{user.lastName}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action soft-deletes the user {user.firstName}
				{user.lastName}. Soft-deletion can only be recovered through direct database access.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action onclick={deleteUser_} class={buttonVariants({ variant: 'destructive' })}>
				Continue
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
