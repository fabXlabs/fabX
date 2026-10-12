<script lang="ts">
	// noinspection ES6UnusedImports
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '#lib/components/ui/button/index.js';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
	import type { Tool } from '#lib/api/model/tool.js';
	import type { AugmentedDevice } from '#lib/api/model/device.js';
	import UnlockToolAlert from './UnlockToolAlert.svelte';
	import DetachToolAlert from './DetachToolAlert.svelte';

	interface Props {
		device: AugmentedDevice;
		pin: number;
		tool: Tool;
	}

	let { device, pin, tool }: Props = $props();

	let showUnlockToolAlert = $state(false);
	let showDetachToolAlert = $state(false);

	function showUnlockToolAlert_() {
		showUnlockToolAlert = true;
	}

	function showDetachToolAlert_() {
		showDetachToolAlert = true;
	}
</script>

<UnlockToolAlert bind:open={showUnlockToolAlert} {device} {tool} />
<DetachToolAlert bind:open={showDetachToolAlert} {device} {pin} {tool} />
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
			onclick={showUnlockToolAlert_}
			class="text-red-600 data-highlighted:text-red-500"
		>
			Unlock
		</DropdownMenu.Item>
		<DropdownMenu.Item
			onclick={showDetachToolAlert_}
			class="text-red-600 data-highlighted:text-red-500"
		>
			Detach
		</DropdownMenu.Item>
	</DropdownMenu.Content>
</DropdownMenu.Root>
