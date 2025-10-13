<script lang="ts">
	import type { FabXError } from '$lib/api/model/error';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import { Button } from '$lib/components/ui/button';
	import AddUsernamePasswordIdentitySheet from './AddUsernamePasswordIdentitySheet.svelte';
	import AddCardIdentitySheet from './AddCardIdentitySheet.svelte';
	import AddCardIdentityAtDeviceSheet from './AddCardIdentityAtDeviceSheet.svelte';
	import type { Device } from '$lib/api/model/device';
	import AddPinIdentitySheet from './AddPinIdentitySheet.svelte';
	import AddPhoneNrIdentitySheet from './AddPhoneNrIdentitySheet.svelte';
	import type { AugmentedUser } from '$lib/api/model/user';

	interface Props {
		user: AugmentedUser;
		error: FabXError | null;
		devices: Device[];
	}

	let { user, error = $bindable(), devices }: Props = $props();

	let addUsernamePasswordIdentitySheetOpen = $state(false);
	let addCardIdentitySheetOpen = $state(false);
	let addCardIdentityAtDeviceSheetOpen = $state(false);
	let addPinIdentitySheetOpen = $state(false);
	let addPhoneNrIdentitySheetOpen = $state(false);

	function hasUsernamePasswordIdentity(): boolean {
		return !!user.identities.find(
			(i) => i.type == 'cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity'
		);
	}
	function hasPinIdentity(): boolean {
		return !!user.identities.find((i) => i.type == 'cloud.fabX.fabXaccess.user.rest.PinIdentity');
	}
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline">Add</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end">
		<DropdownMenu.Group>
			{#if !hasUsernamePasswordIdentity()}
				<DropdownMenu.Item onclick={() => (addUsernamePasswordIdentitySheetOpen = true)}>
					Username / Password
				</DropdownMenu.Item>
			{/if}
			<DropdownMenu.Item onclick={() => (addCardIdentitySheetOpen = true)}>Card</DropdownMenu.Item>
			<DropdownMenu.Item onclick={() => (addCardIdentityAtDeviceSheetOpen = true)}>
				Card (at Device)
			</DropdownMenu.Item>
			{#if !hasPinIdentity()}
				<DropdownMenu.Item onclick={() => (addPinIdentitySheetOpen = true)}>Pin</DropdownMenu.Item>
			{/if}
			<DropdownMenu.Item onclick={() => (addPhoneNrIdentitySheetOpen = true)}>
				Phone Nr.
			</DropdownMenu.Item>
		</DropdownMenu.Group>
	</DropdownMenu.Content>
</DropdownMenu.Root>

<AddUsernamePasswordIdentitySheet
	bind:sheetOpen={addUsernamePasswordIdentitySheetOpen}
	userId={user.id}
/>
<AddCardIdentitySheet bind:sheetOpen={addCardIdentitySheetOpen} userId={user.id} />
<AddCardIdentityAtDeviceSheet
	bind:sheetOpen={addCardIdentityAtDeviceSheetOpen}
	userId={user.id}
	{devices}
/>
<AddPinIdentitySheet bind:sheetOpen={addPinIdentitySheetOpen} userId={user.id} />
<AddPhoneNrIdentitySheet bind:sheetOpen={addPhoneNrIdentitySheetOpen} userId={user.id} />
