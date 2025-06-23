<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { Label } from '$lib/components/ui/label';
	import { addUsernamePasswordIdentity } from '$lib/api/users';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		sheetOpen: boolean;
		userId: string;
	}

	let { sheetOpen = $bindable(false), userId }: Props = $props();

	let username = $state('');
	let password = $state('');

	let error: FabXError | null = $state(null);

	// reset form when opening sheet
	$effect(() => {
		if (sheetOpen) {
			resetForm();
		}
	});

	async function submit() {
		error = null;

		const res = await addUsernamePasswordIdentity(fetch, userId, username, password).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await invalidateAll();
		}
	}

	function resetForm() {
		username = '';
		password = '';
		error = null;
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add Username/Password Identity</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="username">Username</Label>
					<Input id="username" bind:value={username} />
				</div>
				<div class="grid gap-2">
					<Label for="password">Password</Label>
					<Input id="password" type="password" autocomplete="new-password" bind:value={password} />
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Add</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
