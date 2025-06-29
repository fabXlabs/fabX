<script lang="ts">
	// noinspection ES6UnusedImports
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '$lib/components/ui/button';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import type { FabXError } from '$lib/api/model/error';
	import { invalidateAll } from '$app/navigation';
	import { removeMemberQualification } from '$lib/api/users';

	interface Props {
		userId: string;
		qualificationId: string;
		error: FabXError | null;
	}

	let { userId, qualificationId, error = $bindable() }: Props = $props();

	async function removeQualification(): Promise<string> {
		const res = await removeMemberQualification(fetch, userId, qualificationId).catch((e) => {
			error = e;
			return '';
		});
		await invalidateAll();
		return res;
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
		<DropdownMenu.Item
			onclick={removeQualification}
			class="text-red-600 data-highlighted:text-red-500"
		>
			Delete
		</DropdownMenu.Item>
	</DropdownMenu.Content>
</DropdownMenu.Root>
