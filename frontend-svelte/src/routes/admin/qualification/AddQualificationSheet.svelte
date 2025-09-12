<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import { Plus } from 'lucide-svelte';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { addQualification } from '$lib/api/qualifications';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';

	let sheetOpen = $state(false);

	let name = $state('');
	let description = $state('');
	let colour = $state(''); // hex colour string
	let orderNr = $state(0);

	let error: FabXError | null = $state(null);

	async function submit() {
		error = null;

		const res = await addQualification(fetch, {
			name,
			description,
			colour,
			orderNr
		}).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await goto(resolve('/admin/qualification/[id]', { id: res }));
		}
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Trigger class={buttonVariants({ variant: 'normalcase' })}>
		<Plus />
		Add Qualification
	</Sheet.Trigger>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add Qualification</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="name">Name</Label>
					<Input id="name" bind:value={name} />
				</div>
				<div class="grid gap-2">
					<Label for="description">Description</Label>
					<Input id="description" bind:value={description} />
				</div>
				<div class="grid gap-2">
					<Label for="colour">Colour</Label>
					<Input id="colour" type="color" bind:value={colour} />
				</div>
				<div class="grid gap-2">
					<Label for="orderNr">Order Nr.</Label>
					<Input id="orderNr" bind:value={orderNr} />
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Add</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
