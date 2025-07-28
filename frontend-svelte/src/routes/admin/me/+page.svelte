<script lang="ts">
	import type { PageProps } from './$types';
	import { Button } from '$lib/components/ui/button';
	import { addWebauthnIdentity } from '$lib/api/users';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { FabXError } from '$lib/api/model/error';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();

	let error: FabXError | null = $state(null);

	async function addPasskey() {
		error = null;

		await addWebauthnIdentity(fetch, data.me.id).catch((e) => {
			error = e;
			return '';
		});
	}
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.me}
		<Crumbs />
		<h1 class="font-accent mt-4 mb-2 text-3xl">Profile</h1>
		<ErrorText {error} />
		<div class="py-4">
			<Button onclick={addPasskey}>Add Passkey</Button>
		</div>
	{/if}
</div>
