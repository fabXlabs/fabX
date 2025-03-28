<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import NavLink from './NavLink.svelte';
	import { Menu } from 'lucide-svelte';
	import SiteHeaderDropdownMenu from '$lib/components/SiteHeaderDropdownMenu.svelte';

	let sheetOpen = $state(false);
</script>

<header class="border-border/40 bg-background/95 supports-backdrop-filter:bg-background/60 sticky top-0 z-50 w-full border-b backdrop-blur-sm">
	<div class="container flex h-14 max-w-(--breakpoint-2xl) items-center font-mono text-2xl">
		<div class="mr-4 flex">
			<!-- Mobile Nav -->
			<Sheet.Root bind:open={sheetOpen}>
				<Sheet.Trigger class="mr-2 px-0 text-center text-base hover:bg-transparent focus-visible:bg-transparent focus-visible:ring-0 focus-visible:ring-offset-0 sm:hidden outline-hidden">
					<Menu size="32" class="mt-1 mr-2 hover:text-foreground/80 transition-colors text-foreground/60"/>
					<span class="sr-only">Toggle Menu</span>
				</Sheet.Trigger>
				<Sheet.Content side="left" class="flex flex-col text-2xl">
					<span class="font-bold text-violet-800 transition-colors font-accent italic text-4xl">fabX</span>
					<NavLink bind:sheetOpen={sheetOpen} classes="mt-4" href="/admin/user">Users</NavLink>
					<NavLink bind:sheetOpen={sheetOpen} classes="mt-4" href="/admin/tool">Tools</NavLink>
					<NavLink bind:sheetOpen={sheetOpen} classes="mt-4" href="/admin/qualification">Qualifications</NavLink>
					<NavLink bind:sheetOpen={sheetOpen} classes="mt-4" href="/admin/device">Devices</NavLink>
				</Sheet.Content>
			</Sheet.Root>

			<!-- Main Nav -->
			<a href="/admin" class="mr-6 flex items-center space-x-2">
				<span class="font-bold hover:text-violet-800 transition-colors font-accent italic">fabX</span>
			</a>
			<nav class="hidden sm:flex items-center gap-6 text-base">
				<NavLink href="/admin/user">Users</NavLink>
				<NavLink href="/admin/tool">Tools</NavLink>
				<NavLink href="/admin/qualification">Qualifications</NavLink>
				<NavLink href="/admin/device">Devices</NavLink>
			</nav>
		</div>
		<div class="flex flex-1 items-center space-x-2 justify-end">
			<nav class="flex items-center gap-6 text-base">
				<SiteHeaderDropdownMenu />
			</nav>
		</div>
	</div>
</header>
