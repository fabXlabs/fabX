<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { AugmentedUser } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import { changeIsAdmin } from '$lib/api/users';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();

	let open: boolean = $state(false);
	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function changeIsAdmin_() {
		working = true;
		error = null;

		const res = await changeIsAdmin(fetch, user.id, !user.isAdmin).catch((e) => {
			error = e;
			working = false;
			return '';
		});

		if (res) {
			reset();
			await invalidateAll();
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
			<Button {...props} variant="outline">
				{#if user.isAdmin}
					Remove Admin Rights
				{:else}
					Add Admin Rights
				{/if}
			</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				{#if user.isAdmin}
					Remove Admin Rights from {user.firstName} {user.lastName}?
				{:else}
					Add Admin Rights to {user.firstName} {user.lastName}?
				{/if}
			</AlertDialog.Title>
			<AlertDialog.Description>
				{#if user.isAdmin}
					<span
						>This action fully removes admin rights from {user.firstName}
						{user.lastName}. They may still be instructor for qualifications and be able to add
						these qualifications as member qualifications to other users.</span
					>
				{:else}
					<span
						>This gives full admin rights to {user.firstName}
						{user.lastName}. Enables them to see all data in this fabX installation, change all
						configuration and add instructor qualifications to themselves or other members.</span
					>
				{/if}
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={changeIsAdmin_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Continue
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
