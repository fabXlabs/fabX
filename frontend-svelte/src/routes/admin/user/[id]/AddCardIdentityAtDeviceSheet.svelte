<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	// noinspection ES6UnusedImports
	import * as Select from '$lib/components/ui/select/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { Loader2Icon } from 'lucide-svelte';
	import type { Device } from '$lib/api/model/device';
	import { addCardIdentityAtDevice } from '$lib/api/devices';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		sheetOpen: boolean;
		userId: string;
		devices: Device[];
	}

	let { sheetOpen = $bindable(false), userId, devices }: Props = $props();

	let deviceId: string | undefined = $state(undefined);

	const triggerContent = $derived(
		devices.find((d) => d.id === deviceId)?.name ?? 'Select a Device'
	);

	let requestRunning = $state(false);
	let error: FabXError | null = $state(null);

	// reset form when opening sheet
	$effect(() => {
		if (sheetOpen) {
			resetForm();
		}
	});

	async function submit() {
		error = null;
		requestRunning = true;

		const res = await addCardIdentityAtDevice(fetch, deviceId || '', userId).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await invalidateAll();
		}

		requestRunning = false;
	}

	function resetForm() {
		deviceId = undefined;
		requestRunning = false;
		error = null;
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add Card Identity at Device</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<Select.Root type="single" name="device" bind:value={deviceId}>
					<Select.Trigger class="w-full">{triggerContent}</Select.Trigger>
					<Select.Content>
						<Select.Group>
							{#each devices as device (device.id)}
								<Select.Item value={device.id} label={device.name}>
									{device.name}
								</Select.Item>
							{/each}
						</Select.Group>
					</Select.Content>
				</Select.Root>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				{#if requestRunning}
					<Button disabled>
						<Loader2Icon class="animate-spin" />
						Add
					</Button>
				{:else}
					<Button type="submit" class="w-full">Add</Button>
				{/if}
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
