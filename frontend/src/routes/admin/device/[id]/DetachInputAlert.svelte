<script lang="ts">
	// noinspection ES6UnusedImports
	import * as AlertDialog from '$lib/components/ui/alert-dialog/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { buttonVariants } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { AugmentedDevice, InputDescription } from '$lib/api/model/device';
	import { detachInput } from '$lib/api/devices';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		device: AugmentedDevice;
		pin: number;
		open: boolean;
	}

	let { device, pin, open = $bindable() }: Props = $props();

	let inputDescription: InputDescription = $derived.by(() => {
		if (pin in device.attachedInputs) {
			return device.attachedInputs[pin];
		} else {
			return {
				name: 'Unknown Input',
				descriptionLow: '',
				descriptionHigh: '',
				colourLow: '',
				colourHigh: ''
			};
		}
	});

	let working = $state(false);

	let error: FabXError | null = $state(null);

	async function detachInput_(): Promise<string> {
		working = true;
		error = null;

		const res = await detachInput(fetch, device.id, pin)
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
				Detach {inputDescription.name} from {device.name}?
			</AlertDialog.Title>
			<AlertDialog.Description>
				This action detaches the input {inputDescription.name} from the device {device.name} at pin {pin}.
				<ErrorText {error} />
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={reset}>Cancel</AlertDialog.Cancel>
			<AlertDialog.ActionWorking
				onclick={detachInput_}
				class={buttonVariants({ variant: 'destructive' })}
				{working}
			>
				Detach Input
			</AlertDialog.ActionWorking>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
