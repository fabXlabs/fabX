<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { AugmentedUser } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { goto } from '$app/navigation';
	import { deleteUser } from '$lib/api/users.js';
	import { resolve } from '$app/paths';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();

	let open = $state(false);
	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function deleteUser_() {
		working = true;
		error = null;

		const res = await deleteUser(fetch, user.id).catch((e) => {
			error = e;
			working = false;
			return '';
		});

		if (res) {
			reset();
			await goto(resolve(`/admin/user/`));
		}
	}

	function reset() {
		open = false;
		working = false;
		error = null;
	}
</script>

<AlertDialog.Root bind:open>
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
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={deleteUser_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Continue
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
