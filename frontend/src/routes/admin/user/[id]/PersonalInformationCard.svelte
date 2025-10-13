<script lang="ts">
	import { Label } from '$lib/components/ui/label/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { Button } from '$lib/components/ui/button/index.js';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { AugmentedUser, UserDetails } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { changePersonalInformation } from '$lib/api/users';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();

	let editing = $state(false);

	let firstName = $state(user.firstName);
	let lastName = $state(user.lastName);
	let wikiName = $state(user.wikiName);

	let error: FabXError | null = $state(null);

	function resetForm() {
		firstName = user.firstName;
		lastName = user.lastName;
		wikiName = user.wikiName;
		error = null;
	}

	function toggleEditing() {
		editing = !editing;
		if (!editing) {
			resetForm();
		}
	}

	async function submit() {
		const details: UserDetails = {
			firstName: firstName != user.firstName ? { newValue: firstName } : null,
			lastName: lastName != user.lastName ? { newValue: lastName } : null,
			wikiName: wikiName != user.wikiName ? { newValue: wikiName } : null
		};

		const res = await changePersonalInformation(fetch, user.id, details).catch((e) => {
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
			<Card.Title class="text-lg">Personal Information</Card.Title>
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
					<Label for="firstName" class="text-muted-foreground">First Name</Label>
					<Input
						id="firstName"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={firstName}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="lastName" class="text-muted-foreground">Last Name</Label>
					<Input
						id="lastName"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={lastName}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="wikiName" class="text-muted-foreground">Wiki Name</Label>
					<Input
						id="wikiName"
						class="disabled:border-transparent disabled:opacity-100"
						autocorrect="off"
						autocapitalize="off"
						autocomplete="off"
						spellcheck="false"
						disabled={!editing}
						bind:value={wikiName}
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
