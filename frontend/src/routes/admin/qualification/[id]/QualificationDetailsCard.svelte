<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { Qualification, QualificationDetails } from '$lib/api/model/qualification';
	import type { FabXError } from '$lib/api/model/error';
	import { changeQualificationDetails } from '$lib/api/qualifications';
	import { invalidateAll } from '$app/navigation';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import ErrorText from '$lib/components/ErrorText.svelte';

	interface Props {
		qualification: Qualification;
	}

	let { qualification }: Props = $props();

	let editing = $state(false);

	let name = $state(qualification.name);
	let description = $state(qualification.description);
	let colour = $state(qualification.colour);
	let orderNr = $state(qualification.orderNr);

	let error: FabXError | null = $state(null);

	function resetForm() {
		name = qualification.name;
		description = qualification.description;
		colour = qualification.colour;
		orderNr = qualification.orderNr;
	}

	function toggleEditing() {
		editing = !editing;
		if (!editing) {
			resetForm();
		}
	}

	async function submit() {
		const details: QualificationDetails = {
			name: name != qualification.name ? { newValue: name } : null,
			description: description != qualification.description ? { newValue: description } : null,
			colour: colour != qualification.colour ? { newValue: colour } : null,
			orderNr: orderNr != qualification.orderNr ? { newValue: orderNr } : null
		};

		const res = await changeQualificationDetails(fetch, qualification.id, details).catch((e) => {
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
			<Card.Title class="text-lg">Qualification Details</Card.Title>
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
					<Label for="description" class="text-muted-foreground">Description</Label>
					<Input
						id="description"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={description}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="colour" class="text-muted-foreground">Colour</Label>
					<Input
						id="colour"
						type="color"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={colour}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="orderNr" class="text-muted-foreground">Order Nr.</Label>
					<Input
						id="orderNr"
						inputmode="numeric"
						pattern={'\\d{1,100}'}
						min="0"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={orderNr}
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
