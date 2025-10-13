<script lang="ts">
	// noinspection ES6UnusedImports
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '$lib/components/ui/button/index.js';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import HardDeleteUserAlert from './HardDeleteUserAlert.svelte';
	import type { User } from '$lib/api/model/user';

	interface Props {
		user: User;
	}

	let { user }: Props = $props();

	let alertOpen = $state(false);
</script>

<div class="text-right">
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
				onclick={() => (alertOpen = true)}
				class="text-red-600 data-highlighted:text-red-500"
			>
				Hard Delete
			</DropdownMenu.Item>
		</DropdownMenu.Content>
	</DropdownMenu.Root>
	<HardDeleteUserAlert {user} bind:open={alertOpen} />
</div>
