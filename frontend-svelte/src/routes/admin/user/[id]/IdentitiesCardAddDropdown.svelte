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

	interface Props {
		userId: string;
		error: FabXError | null;
		devices: Device[];
	}

	let { userId, error = $bindable(), devices }: Props = $props();

	let addUsernamePasswordIdentitySheetOpen = $state(false);
	let addCardIdentitySheetOpen = $state(false);
	let addCardIdentityAtDeviceSheetOpen = $state(false);
	let addPinIdentitySheetOpen = $state(false);
	let addPhoneNrIdentitySheetOpen = $state(false);
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline">Add</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end">
		<DropdownMenu.Group>
			<!-- TODO hide entries based on existing identities -->
			<DropdownMenu.Item onclick={() => (addUsernamePasswordIdentitySheetOpen = true)}>
				Username / Password
			</DropdownMenu.Item>
			<DropdownMenu.Item onclick={() => (addCardIdentitySheetOpen = true)}>Card</DropdownMenu.Item>
			<DropdownMenu.Item onclick={() => (addCardIdentityAtDeviceSheetOpen = true)}>
				Card (at Device)
			</DropdownMenu.Item>
			<DropdownMenu.Item onclick={() => (addPinIdentitySheetOpen = true)}>Pin</DropdownMenu.Item>
			<DropdownMenu.Item onclick={() => (addPhoneNrIdentitySheetOpen = true)}>
				Phone Nr.
			</DropdownMenu.Item>
		</DropdownMenu.Group>
	</DropdownMenu.Content>
</DropdownMenu.Root>

<AddUsernamePasswordIdentitySheet bind:sheetOpen={addUsernamePasswordIdentitySheetOpen} {userId} />
<AddCardIdentitySheet bind:sheetOpen={addCardIdentitySheetOpen} {userId} />
<AddCardIdentityAtDeviceSheet
	bind:sheetOpen={addCardIdentityAtDeviceSheetOpen}
	{userId}
	{devices}
/>
<AddPinIdentitySheet bind:sheetOpen={addPinIdentitySheetOpen} {userId} />
<AddPhoneNrIdentitySheet bind:sheetOpen={addPhoneNrIdentitySheetOpen} {userId} />
