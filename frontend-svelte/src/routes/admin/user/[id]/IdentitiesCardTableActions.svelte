<script lang="ts">
	// noinspection ES6UnusedImports
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '$lib/components/ui/button';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import type { UserIdentity } from '$lib/api/model/user';
	import {
		removeCardIdentity,
		removePhoneIdentity,
		removePinIdentity,
		removeUsernamePasswordIdentity
	} from '$lib/api/users';
	import type { FabXError } from '$lib/api/model/error';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		userId: string;
		identity: UserIdentity;
		error: FabXError | null;
	}

	let { userId, identity, error = $bindable() }: Props = $props();

	async function deleteIdentity(): Promise<string> {
		switch (identity.type) {
			case 'cloud.fabX.fabXaccess.user.rest.PinIdentity': {
				const result = await removePinIdentity(fetch, userId);
				await invalidateAll();
				return result;
			}
			case 'cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity': {
				const result = await removeUsernamePasswordIdentity(fetch, userId, identity.username);
				await invalidateAll();
				return result;
			}
			case 'cloud.fabX.fabXaccess.user.rest.CardIdentity': {
				const result = await removeCardIdentity(fetch, userId, identity.cardId);
				await invalidateAll();
				return result;
			}
			case 'cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity': {
				const result = await removePhoneIdentity(fetch, userId, identity.phoneNr);
				await invalidateAll();
				return result;
			}
		}
	}
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="ghost" size="icon" class="relative size-8 p-0">
				<span class="sr-only">Open menu</span>
				<EllipsisIcon />
			</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end">
		<DropdownMenu.Item onclick={deleteIdentity} class="text-red-600 data-highlighted:text-red-500">
			Delete
		</DropdownMenu.Item>
	</DropdownMenu.Content>
</DropdownMenu.Root>
