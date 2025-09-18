<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { AugmentedDevice, DeviceDetails } from '$lib/api/model/device';
	import type { FabXError } from '$lib/api/model/error';
	import { changeDeviceDetails } from '$lib/api/devices';
	import { invalidateAll } from '$app/navigation';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import ErrorText from '$lib/components/ErrorText.svelte';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let editing = $state(false);

	let name = $state(device.name);
	let background = $state(device.background);
	let backupBackendUrl = $state(device.backupBackendUrl);

	let error: FabXError | null = $state(null);

	function resetForm() {
		name = device.name;
		background = device.background;
		backupBackendUrl = device.backupBackendUrl;
		error = null;
	}

	function toggleEditing() {
		editing = !editing;
		if (!editing) {
			resetForm();
		}
	}

	async function submit() {
		const details: DeviceDetails = {
			name: name != device.name ? { newValue: name } : null,
			background: background != device.background ? { newValue: background } : null,
			backupBackendUrl:
				backupBackendUrl != device.backupBackendUrl ? { newValue: backupBackendUrl } : null
		};

		const res = await changeDeviceDetails(fetch, device.id, details).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			editing = false;
			await invalidateAll();
		}
	}
</script>

<Card.Root>
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Device Details</Card.Title>
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
					<Label for="name" class="text-muted-foreground">Name</Label>
					<Input
						id="name"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={name}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="background" class="text-muted-foreground">Background URL</Label>
					<Input
						id="background"
						type="url"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						placeholder="https://example.com/..."
						bind:value={background}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="backupBackendUrl" class="text-muted-foreground">Backup Backend URL</Label>
					<Input
						id="backupBackendUrl"
						type="url"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						placeholder="https://example.com/..."
						bind:value={backupBackendUrl}
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
