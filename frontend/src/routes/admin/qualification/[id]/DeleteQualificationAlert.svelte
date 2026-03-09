<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import type { Qualification } from '$lib/api/model/qualification';
	import { deleteQualification } from '$lib/api/qualifications';

	interface Props {
		qualification: Qualification;
	}

	let { qualification }: Props = $props();

	let open = $state(false);
	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function deleteQualification_(): Promise<string> {
		working = true;
		error = null;

		const res = await deleteQualification(fetch, qualification.id)
			.then((res) => {
				reset();
				return res;
			})
			.catch((e) => {
				error = e;
				working = false;
				return '';
			});

		if (res) {
			reset();
			await goto(resolve(`/admin/qualification/`));
		}

		return res;
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
			<Button {...props} variant="outline">Delete Qualification</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Delete {qualification.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action deletes the qualification {qualification.name}. Deletion cannot be undone.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={deleteQualification_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Continue
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
