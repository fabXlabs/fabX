<script lang="ts">
	import type { PageProps } from './$types';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { FabXError } from '$lib/api/model/error';
	import Crumbs from './Crumbs.svelte';
	import AddPasskeyButton from '$lib/components/AddPasskeyButton.svelte';

	let { data }: PageProps = $props();

	let error: FabXError | null = $state(null);
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.me}
		<Crumbs />
		<h1 class="font-accent mt-4 mb-2 text-3xl">Profile</h1>
		<ErrorText {error} />
		<div class="py-4">
			<AddPasskeyButton me={data.me} bind:error />
		</div>
	{/if}
</div>
