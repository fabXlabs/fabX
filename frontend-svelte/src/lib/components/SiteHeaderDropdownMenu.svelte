<script lang="ts">
	import { resolve } from '$app/paths';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import LogOut from 'lucide-svelte/icons/log-out';
	import { CircleUserRound, User } from 'lucide-svelte';
	import { logout } from '$lib/api/auth';
	import { goto } from '$app/navigation';
	import { getMeContext } from '$lib/context';

	const me = getMeContext();

	const profileLink = me.isAdmin ? '/admin/me' : '/instructor/me';

	async function doLogout() {
		const res = await logout();
		console.log('logout', res);
		await goto(resolve('/'));
	}
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger class="outline-hidden">
		<CircleUserRound class="mt-1" />
	</DropdownMenu.Trigger>
	<DropdownMenu.Content class="w-56" align="end">
		<DropdownMenu.Label class="font-normal">
			<div class="flex flex-col space-y-1">
				<p class="text-base leading-none font-medium">{me.firstName} {me.lastName}</p>
				<p class="text-muted-foreground text-sm leading-none">{me.wikiName}</p>
			</div>
		</DropdownMenu.Label>
		<DropdownMenu.Separator />
		<DropdownMenu.Group>
			<DropdownMenu.Item
				onSelect={async () => {
					await goto(resolve(profileLink));
				}}
				class="cursor-pointer"
			>
				<User class="mr-2 size-4" />
				<span>Profile</span>
			</DropdownMenu.Item>
		</DropdownMenu.Group>
		<DropdownMenu.Separator />
		<DropdownMenu.Item onSelect={doLogout} class="cursor-pointer">
			<LogOut class="mr-2 size-4" />
			<span>Log out</span>
		</DropdownMenu.Item>
	</DropdownMenu.Content>
</DropdownMenu.Root>
