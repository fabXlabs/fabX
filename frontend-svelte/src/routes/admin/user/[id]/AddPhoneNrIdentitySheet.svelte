<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { Label } from '$lib/components/ui/label';
	import { addPhoneIdentity } from '$lib/api/users';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		sheetOpen: boolean;
		userId: string;
	}

	let { sheetOpen = $bindable(false), userId }: Props = $props();

	let phoneNr = $state('');

	let error: FabXError | null = $state(null);

	// reset form when opening sheet
	$effect(() => {
		if (sheetOpen) {
			resetForm();
		}
	});

	async function submit() {
		error = null;

		const res = await addPhoneIdentity(fetch, userId, phoneNr).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await invalidateAll();
		}
	}

	function resetForm() {
		phoneNr = '';
		error = null;
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add Phone Nr. Identity</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="phoneNr">Phone Nr.</Label>
					<Input id="phoneNr" type="tel" bind:value={phoneNr} />
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Add</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
