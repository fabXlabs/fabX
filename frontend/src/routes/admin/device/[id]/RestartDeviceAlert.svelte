<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import { restartDevice } from '$lib/api/devices';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let error: FabXError | null = $state(null);

	async function restartDevice_(): Promise<string> {
		error = null;
		return await restartDevice(fetch, device.id).catch((e) => {
			error = e;
			return '';
		});
	}
</script>

<AlertDialog.Root>
	<AlertDialog.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline">Restart Device</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Restart {device.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This restarts the device {device.name}.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={restartDevice_}
				class={buttonVariants({ variant: 'destructive' })}
			>
				Restart
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
