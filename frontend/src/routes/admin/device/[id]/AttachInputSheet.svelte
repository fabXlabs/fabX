<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { buttonVariants } from '$lib/components/ui/button/index.js';
	import { attachInput } from '$lib/api/devices';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let sheetOpen = $state(false);

	let pin = $state(0);
	let name: string = $state('');
	let descriptionLow: string = $state('');
	let descriptionHigh: string = $state('');
	let colourLow: string = $state('#000000');
	let colourHigh: string = $state('#000000');

	let error: FabXError | null = $state(null);

	async function submit() {
		error = null;

		const res = await attachInput(
			fetch,
			device.id,
			pin,
			name,
			descriptionLow,
			descriptionHigh,
			colourLow,
			colourHigh
		).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await invalidateAll();
			pin = 0;
			name = '';
			descriptionLow = '';
			descriptionHigh = '';
			colourLow = '#000000';
			colourHigh = '#ffffff';
			sheetOpen = false;
		}
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Trigger class={buttonVariants({ variant: 'outline' })}>Attach Input</Sheet.Trigger>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Attach Input</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="pin">Pin</Label>
					<Input id="pin" type="number" min="0" max="15" bind:value={pin} />
				</div>
				<div class="grid gap-2">
					<Label for="name">Name</Label>
					<Input id="name" bind:value={name} />
				</div>
				<div class="grid gap-2">
					<Label for="descriptionLow">Description Low</Label>
					<Input id="descriptionLow" bind:value={descriptionLow} />
				</div>
				<div class="grid gap-2">
					<Label for="descriptionHigh">Description High</Label>
					<Input id="descriptionHigh" bind:value={descriptionHigh} />
				</div>
				<div class="grid gap-2">
					<Label for="colourLow">Colour Low</Label>
					<Input id="colourLow" type="color" bind:value={colourLow} />
				</div>
				<div class="grid gap-2">
					<Label for="colourHigh">Colour High</Label>
					<Input id="colourHigh" type="color" bind:value={colourHigh} />
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Attach</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
