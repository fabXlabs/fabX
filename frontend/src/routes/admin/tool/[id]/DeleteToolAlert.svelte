<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { goto } from '$app/navigation';
	import { deleteTool } from '$lib/api/tools.js';
	import type { AugmentedTool } from '$lib/api/model/tool';
	import { resolve } from '$app/paths';

	interface Props {
		tool: AugmentedTool;
	}

	let { tool }: Props = $props();

	let open = $state(false);
	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function deleteTool_() {
		working = true;
		error = null;

		const res = await deleteTool(fetch, tool.id)
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
			await goto(resolve(`/admin/tool/`));
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
			<Button {...props} variant="outline">Delete Tool</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Delete {tool.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action deletes the tool {tool.name}. Deletion cannot be undone.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={deleteTool_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Continue
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
