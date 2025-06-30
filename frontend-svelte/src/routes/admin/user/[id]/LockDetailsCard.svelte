<script lang="ts">
	import type { AugmentedUser, UserLockDetails } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	import { changeLockState } from '$lib/api/users';
	import { invalidateAll } from '$app/navigation';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Button } from '$lib/components/ui/button';
	import ErrorText from '$lib/components/ErrorText.svelte';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import { Switch } from '$lib/components/ui/switch';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();

	let editing = $state(false);

	let locked = $state(user.locked);
	let notes = $state(user.notes || '');

	let error: FabXError | null = $state(null);

	function resetForm() {
		locked = user.locked;
		notes = user.notes || '';
		error = null;
	}

	function toggleEditing() {
		editing = !editing;
		if (!editing) {
			resetForm();
		}
	}

	async function submit() {
		const normalizedNotes = notes ? notes : null;
		const details: UserLockDetails = {
			locked: locked != user.locked ? { newValue: locked } : null,
			notes: normalizedNotes != user.notes ? { newValue: normalizedNotes } : null
		};

		const res = await changeLockState(user.id, details).catch((e) => {
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
			<Card.Title class="text-lg">Lock</Card.Title>
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
					<Label for="locked" class="text-muted-foreground">Locked</Label>
					<Switch id="locked" disabled={!editing} bind:checked={locked} />
				</div>
				<div class="grid gap-2">
					<Label for="notes" class="text-muted-foreground">Notes</Label>
					<Input
						id="notes"
						class="disabled:border-transparent disabled:opacity-100"
						placeholder="..."
						disabled={!editing}
						bind:value={notes}
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
