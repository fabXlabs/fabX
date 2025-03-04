<script lang="ts">
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import LogOut from 'lucide-svelte/icons/log-out';
	import { CircleUserRound, User } from 'lucide-svelte';
	import { logout } from '$lib/api/auth';
	import { goto } from '$app/navigation';

	async function doLogout() {
		const res = await logout();
		console.log('logout', res);
		await goto('/');
	}
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		<CircleUserRound class="mt-1" />
	</DropdownMenu.Trigger>
	<DropdownMenu.Content class="w-56" align="end">
		<DropdownMenu.Label class="font-normal">
			<div class="flex flex-col space-y-1">
				<!-- TODO show acting user details -->
				<p class="text-base font-medium leading-none">First Last</p>
				<p class="text-muted-foreground text-sm leading-none">wikiname</p>
			</div>
		</DropdownMenu.Label>
		<DropdownMenu.Separator />
		<DropdownMenu.Group>
			<DropdownMenu.Item>
				<User class="mr-2 size-4" />
				<span>Profile</span>
			</DropdownMenu.Item>
		</DropdownMenu.Group>
		<DropdownMenu.Separator />
		<DropdownMenu.Item onSelect={doLogout} class="cursor-pointer">
			<LogOut class="mr-2 size-4" />
			<span>Log out</span>
		</DropdownMenu.Item>
		<!--		<DropdownMenu.Item class="block">-->
		<!--			<a href="/logout">-->
		<!--				<div class="w-full flex gap-2 items-center">-->
		<!--					<LogOut class="mr-2 size-4" />-->
		<!--					<span>Log out</span>-->
		<!--				</div>-->
		<!--			</a>-->
		<!--		</DropdownMenu.Item>-->
	</DropdownMenu.Content>
</DropdownMenu.Root>