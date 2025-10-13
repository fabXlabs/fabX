<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import { unlockTool } from '$lib/api/devices';
	import type { Tool } from '$lib/api/model/tool';

	interface Props {
		device: AugmentedDevice;
		tool: Tool;
		open: boolean;
	}

	let { device, tool, open = $bindable() }: Props = $props();

	let error: FabXError | null = $state(null);

	async function unlockTool_(): Promise<string> {
		error = null;
		return await unlockTool(fetch, device.id, tool.id).catch((e) => {
			error = e;
			return '';
		});
	}
</script>

<AlertDialog.Root bind:open>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Unlock {tool.name} at {device.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action unlocks the tool {tool.name} at the device {device.name}.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action onclick={unlockTool_} class={buttonVariants({ variant: 'destructive' })}>
				Unlock Tool
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
