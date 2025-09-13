<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import { Plus } from 'lucide-svelte';
	import { Input } from '$lib/components/ui/input';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { Label } from '$lib/components/ui/label';
	import { addDevice } from '$lib/api/devices';
	import { resolve } from '$app/paths';
	import { goto } from '$app/navigation';

	let sheetOpen = $state(false);

	let name = $state('');
	let background = $state('');
	let backupBackendUrl = $state('');
	let mac = $state('');
	let secret = $state('');

	let error: FabXError | null = $state(null);

	async function submit() {
		error = null;

		const res = await addDevice(fetch, {
			name,
			background,
			backupBackendUrl,
			mac,
			secret
		}).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await goto(resolve('/admin/device/[id]', { id: res }));
		}
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Trigger class={buttonVariants({ variant: 'normalcase' })}>
		<Plus />
		Add Device
	</Sheet.Trigger>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add Device</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="name">Name</Label>
					<Input id="name" bind:value={name} />
				</div>
				<div class="grid gap-2">
					<Label for="background">Background URL</Label>
					<Input id="background" type="url" bind:value={background} />
				</div>
				<div class="grid gap-2">
					<Label for="backupBackendUrl">Backup Backend URL</Label>
					<Input id="backupBackendUrl" type="url" bind:value={backupBackendUrl} />
				</div>
				<div class="grid gap-2">
					<Label for="mac">MAC</Label>
					<Input id="mac" bind:value={mac} />
				</div>
				<div class="grid gap-2">
					<Label for="secret">Secret</Label>
					<Input id="secret" bind:value={secret} />
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Add</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
