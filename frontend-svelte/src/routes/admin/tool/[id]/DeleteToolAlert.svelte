<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { goto } from '$app/navigation';
	import { deleteTool } from '$lib/api/tools.js';
	import type { AugmentedTool } from '$lib/api/model/tool';

	interface Props {
		tool: AugmentedTool;
	}

	let { tool }: Props = $props();

	let error: FabXError | null = $state(null);

	async function deleteTool_() {
		error = null;
		const res = await deleteTool(fetch, tool.id).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await goto(`/admin/tool/`);
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
				Delete {tool.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action deletes the tool {tool.name}. Deletion cannot be undone.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action onclick={deleteTool_} class={buttonVariants({ variant: 'destructive' })}>
				Continue
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
