<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import { deleteDevice } from '$lib/api/devices';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let error: FabXError | null = $state(null);

	async function deleteDevice_() {
		error = null;
		const res = await deleteDevice(fetch, device.id).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await goto(resolve(`/admin/device/`));
		}
	}
</script>

<AlertDialog.Root>
	<AlertDialog.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline">Delete Device</Button>
		{/snippet}
	</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>
				Delete {device.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action deletes the device {device.name}. Deletion cannot be undone.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={deleteDevice_}
				class={buttonVariants({ variant: 'destructive' })}
			>
				Continue
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
