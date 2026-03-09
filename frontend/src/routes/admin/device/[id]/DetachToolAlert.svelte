<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import { detachTool } from '$lib/api/devices';
	import type { Tool } from '$lib/api/model/tool';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		device: AugmentedDevice;
		pin: number;
		tool: Tool;
		open: boolean;
	}

	let { device, pin, tool, open = $bindable() }: Props = $props();

	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function detachTool_(): Promise<string> {
		working = true;
		error = null;

		const res = await detachTool(fetch, device.id, pin)
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
			await invalidateAll();
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
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Detach {tool.name} from {device.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action detaches the tool {tool.name} from the device {device.name} at pin {pin}.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={detachTool_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Detach Tool
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
