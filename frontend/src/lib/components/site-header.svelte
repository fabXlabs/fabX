<header class="border-border/40 bg-background/95 supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50 w-full border-b backdrop-blur">
	<div class="container flex h-14 max-w-screen-2xl items-center font-mono text-xl">
		<!-- Main Nav -->
		<div class="mr-4 flex">
			<a href="/admin" class="mr-6 flex items-center space-x-2">
				<span class="font-bold hover:text-violet-800 transition-colors">fabX</span>
			</a>
			<nav class="flex items-center gap-6 text-sm">
				<a class="hover:text-foreground/80 transition-colors text-foreground/60" href="/admin/user">Users</a>
				<a class="hover:text-foreground/80 transition-colors text-foreground/60" href="/admin/tool">Tools</a>
				<a class="hover:text-foreground/80 transition-colors text-foreground/60" href="/admin/qualification">Qualifications</a>
				<a class="hover:text-foreground/80 transition-colors text-foreground/60" href="/admin/device">Devices</a>
			</nav>
		</div>
		<!-- TODO Mobile Nav -->
		<div class="flex flex-1 items-center justify-between space-x-2 md:justify-end">
			<nav class="flex items-center gap-6 text-sm">
				<!-- TODO User/Profile Icon -->
				<a class="hover:text-foreground/80 transition-colors text-foreground/60" href="/me">Profile</a>
			</nav>
		</div>
	</div>
</header>
