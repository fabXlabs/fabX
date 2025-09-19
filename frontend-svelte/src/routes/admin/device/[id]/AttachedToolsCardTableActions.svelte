<script lang="ts">
	// noinspection ES6UnusedImports
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '$lib/components/ui/button';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import type { FabXError } from '$lib/api/model/error';
	import { invalidateAll } from '$app/navigation';
	import { detachTool, unlockTool } from '$lib/api/devices';

	interface Props {
		deviceId: string;
		pin: number;
		toolId: string;
		error: FabXError | null;
	}

	let { deviceId, pin, toolId, error = $bindable() }: Props = $props();

	async function detachTool_(): Promise<string> {
		const res = await detachTool(fetch, deviceId, pin).catch((e) => {
			error = e;
			return '';
		});
		await invalidateAll();
		return res;
	}

	async function unlockTool_(): Promise<string> {
		// TODO success notification
		const res = await unlockTool(fetch, deviceId, toolId).catch((e) => {
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
		<!-- TODO Alert to ask whether the actor really wants to unlock the tool -->
		<DropdownMenu.Item onclick={unlockTool_} class="text-red-600 data-highlighted:text-red-500">
			Unlock
		</DropdownMenu.Item>
		<!-- TODO Alert to ask whether the actor really wants to detach the tool -->
		<DropdownMenu.Item onclick={detachTool_} class="text-red-600 data-highlighted:text-red-500">
			Detach
		</DropdownMenu.Item>
	</DropdownMenu.Content>
</DropdownMenu.Root>
