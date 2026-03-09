<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import { updateDeviceFirmware } from '$lib/api/devices';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let open = $state(false);
	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function updateDeviceFirmware_(): Promise<string> {
		working = true;
		error = null;

		return await updateDeviceFirmware(fetch, device.id)
			.then((res) => {
				reset();
				return res;
			})
			.catch((e) => {
				error = e;
				working = false;
				return '';
			});
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
			<Button {...props} variant="outline">Update Device Firmware</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Update Firmware of {device.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This tells the device {device.name} to download its firmware and restart.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={updateDeviceFirmware_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Update and Restart
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
