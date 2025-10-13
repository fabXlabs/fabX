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

	let error: FabXError | null = $state(null);

	async function deleteQualification_() {
		error = null;
		const res = await deleteQualification(fetch, qualification.id).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await goto(resolve(`/admin/qualification/`));
		}
	}
</script>

<AlertDialog.Root>
	<AlertDialog.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline">Delete Tool</Button>
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
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={deleteQualification_}
				class={buttonVariants({ variant: 'destructive' })}
			>
				Continue
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
