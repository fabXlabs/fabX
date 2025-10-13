<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { AugmentedDevice, DesiredFirmwareVersion } from '$lib/api/model/device';
	import type { FabXError } from '$lib/api/model/error';
	import { changeDesiredFirmwareVersion } from '$lib/api/devices';
	import { invalidateAll } from '$app/navigation';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import ErrorText from '$lib/components/ErrorText.svelte';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let editing = $state(false);

	let desiredFirmwareVersion = $state(device.desiredFirmwareVersion);

	let error: FabXError | null = $state(null);

	function resetForm() {
		desiredFirmwareVersion = device.desiredFirmwareVersion;
		error = null;
	}

	function toggleEditing() {
		editing = !editing;
		if (!editing) {
			resetForm();
		}
	}

	async function submit() {
		if (desiredFirmwareVersion != device.desiredFirmwareVersion) {
			const details: DesiredFirmwareVersion = {
				desiredFirmwareVersion: desiredFirmwareVersion ?? ''
			};

			const res = await changeDesiredFirmwareVersion(fetch, device.id, details).catch((e) => {
				error = e;
				return '';
			});

			if (res) {
				editing = false;
				await invalidateAll();
			}
		} else {
			editing = false;
		}
	}
</script>

<Card.Root>
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Device Firmware</Card.Title>
			<Button variant="outline" onclick={toggleEditing}>
				{#if !editing}
					Edit
				{:else}
					Cancel
				{/if}
			</Button>
		</div>
	</Card.Header>
	<Card.Content>
		<form onsubmit={submit}>
			<div class="grid gap-4">
				<div class="grid gap-2">
					<Label class="text-muted-foreground">Actual Firmware Version</Label>
					<Input
						id="actualFirmwareVersion"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={true}
						placeholder="unknown"
						bind:value={device.actualFirmwareVersion}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="desiredFirmwareVersion" class="text-muted-foreground"
						>Desired Firmware Version</Label
					>
					<Input
						id="desiredFirmwareVersion"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={desiredFirmwareVersion}
					/>
				</div>
				{#if editing}
					<ErrorText {error} />
					<Button type="submit" class="w-full">Save</Button>
				{/if}
			</div>
		</form>
	</Card.Content>
</Card.Root>
