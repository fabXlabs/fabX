<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import { UserRoundPlus } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { addUser } from '$lib/api/users';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';

	let sheetOpen = $state(false);

	let firstName = $state('');
	let lastName = $state('');
	let wikiName = $state('');

	let error: FabXError | null = $state(null);

	async function submit() {
		error = null;

		const res = await addUser({
			firstName,
			lastName,
			wikiName
		}).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await goto(`${base}/admin/user/${res}`);
		}
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Trigger
		class="mr-2 px-0 text-center text-base outline-hidden hover:bg-transparent focus-visible:bg-transparent focus-visible:ring-0 focus-visible:ring-offset-0"
	>
		<Button class="normal-case">
			<UserRoundPlus />
			Add User
		</Button>
	</Sheet.Trigger>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add User</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="firstName">First Name</Label>
					<Input id="firstName" bind:value={firstName} />
				</div>
				<div class="grid gap-2">
					<Label for="lastName">Last Name</Label>
					<Input id="lastName" bind:value={lastName} />
				</div>
				<div class="grid gap-2">
					<Label for="wikiName">Wiki Name</Label>
					<Input id="wikiName" bind:value={wikiName} />
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Add</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
