<script lang="ts">
	// noinspection ES6UnusedImports
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '#lib/components/ui/button/index.js';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
	import type { AugmentedDevice } from '#lib/api/model/device.js';
	import DetachInputAlert from './DetachInputAlert.svelte';

	interface Props {
		device: AugmentedDevice;
		pin: number;
	}

	let { device, pin }: Props = $props();

	let showDetachInputAlert = $state(false);

	function showDetachInputAlert_() {
		showDetachInputAlert = true;
	}
</script>

<DetachInputAlert bind:open={showDetachInputAlert} {device} {pin} />
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
			onclick={showDetachInputAlert_}
			class="text-red-600 data-highlighted:text-red-500"
		>
			Detach
		</DropdownMenu.Item>
	</DropdownMenu.Content>
</DropdownMenu.Root>
