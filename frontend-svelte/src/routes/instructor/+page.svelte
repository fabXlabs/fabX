<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import { base } from '$app/paths';
	import type { PageProps } from './$types';
	import { Drill, Microchip, Stamp, Users } from 'lucide-svelte';

	let { data }: PageProps = $props();
</script>

<svelte:head>
	<title>fabX | Instructor</title>
</svelte:head>


<div class="container relative max-w-(--breakpoint-2xl) mt-7">
	<h2 class="font-accent text-2xl my-2">Hello, {data.me?.firstName}</h2>
</div>

