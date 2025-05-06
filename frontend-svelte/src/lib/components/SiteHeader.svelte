<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import { base } from '$app/paths';
	// noinspection ES6UnusedImports
	import NavLink from './NavLink.svelte';
	import { Menu } from 'lucide-svelte';
	import SiteHeaderDropdownMenu from '$lib/components/SiteHeaderDropdownMenu.svelte';

	let sheetOpen = $state(false);
</script>

<header
	class="border-border/40 bg-background/95 supports-backdrop-filter:bg-background/60 sticky top-0 z-50 w-full border-b backdrop-blur-sm"
>
	<div class="container flex h-14 max-w-(--breakpoint-2xl) items-center font-mono text-2xl">
		<div class="mr-4 flex">
			<!-- Mobile Nav -->
			<Sheet.Root bind:open={sheetOpen}>
				<Sheet.Trigger
					class="mr-2 px-0 text-center text-base outline-hidden hover:bg-transparent focus-visible:bg-transparent focus-visible:ring-0 focus-visible:ring-offset-0 sm:hidden"
				>
					<Menu
						size="32"
						class="hover:text-foreground/80 text-foreground/60 mt-1 mr-2 transition-colors"
					/>
					<span class="sr-only">Toggle Menu</span>
				</Sheet.Trigger>
				<Sheet.Content side="left" class="flex flex-col text-2xl">
					<span class="font-accent text-4xl font-bold text-violet-800 italic transition-colors"
						>fabX</span
					>
					<NavLink bind:sheetOpen classes="mt-4" href="{base}/admin/user">Users</NavLink>
					<NavLink bind:sheetOpen classes="mt-4" href="{base}/admin/tool">Tools</NavLink>
					<NavLink bind:sheetOpen classes="mt-4" href="{base}/admin/qualification"
						>Qualifications</NavLink
					>
					<NavLink bind:sheetOpen classes="mt-4" href="{base}/admin/device">Devices</NavLink>
				</Sheet.Content>
			</Sheet.Root>

			<!-- Main Nav -->
			<a href="{base}/" class="mr-6 flex items-center space-x-2">
				<span class="font-accent font-bold italic transition-colors hover:text-violet-800"
					>fabX</span
				>
			</a>
			<nav class="hidden items-center gap-6 text-base sm:flex">
				<NavLink href="{base}/admin/user">Users</NavLink>
				<NavLink href="{base}/admin/tool">Tools</NavLink>
				<NavLink href="{base}/admin/qualification">Qualifications</NavLink>
				<NavLink href="{base}/admin/device">Devices</NavLink>
			</nav>
		</div>
		<div class="flex flex-1 items-center justify-end space-x-2">
			<nav class="flex items-center gap-6 text-base">
				<SiteHeaderDropdownMenu />
			</nav>
		</div>
	</div>
</header>
