<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from "$lib/components/ui/card/index.js";
	import type { PageProps } from './$types';
	import { Drill, Microchip, Stamp, Users } from 'lucide-svelte';

	let { data }: PageProps = $props();
</script>

<svelte:head>
	<title>fabX | Admin</title>
</svelte:head>

<div class="container relative max-w-screen-2xl mt-7">
	<h2 class="font-accent text-2xl my-2">Hello, {data.me?.firstName}</h2>

	<div class="my-6 grid gap-4 md:grid-cols-2 lg:grid-cols-4">
		<a href="/admin/user">
			<Card.Root>
				<Card.Header
					class="flex flex-row items-center justify-between space-y-0 pb-2"
				>
					<Card.Title class="text-sm font-accent uppercase">Users</Card.Title>
					<Users class="text-muted-foreground h-4 w-4" />
				</Card.Header>
				<Card.Content>
					<div class="text-2xl font-bold"># Users TODO</div>
				</Card.Content>
			</Card.Root>
		</a>
		<a href="/admin/tool">
			<Card.Root>
				<Card.Header
					class="flex flex-row items-center justify-between space-y-0 pb-2"
				>
					<Card.Title class="text-sm font-accent uppercase">Tools</Card.Title>
					<Drill class="text-muted-foreground h-4 w-4" />
				</Card.Header>
				<Card.Content>
					<div class="text-2xl font-bold"># Tools TODO</div>
				</Card.Content>
			</Card.Root>
		</a>
		<a href="/admin/qualification">
			<Card.Root>
				<Card.Header
					class="flex flex-row items-center justify-between space-y-0 pb-2"
				>
					<Card.Title class="text-sm font-accent uppercase">Qualifications</Card.Title>
					<Stamp class="text-muted-foreground h-4 w-4" />
				</Card.Header>
				<Card.Content>
					<div class="text-2xl font-bold"># Qualification TODO</div>
				</Card.Content>
			</Card.Root>
		</a>
		<a href="/admin/device">
			<Card.Root>
				<Card.Header
					class="flex flex-row items-center justify-between space-y-0 pb-2"
				>
					<Card.Title class="text-sm font-accent uppercase">Devices</Card.Title>
					<Microchip class="text-muted-foreground h-4 w-4" />
				</Card.Header>
				<Card.Content>
					<div class="text-2xl font-bold"># Devices TODO</div>
				</Card.Content>
			</Card.Root>
		</a>
	</div>
</div>
