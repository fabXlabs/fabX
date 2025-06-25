<script lang="ts">
	import type { PageProps } from './$types';
	import { Button } from '$lib/components/ui/button';
	import { addWebauthnIdentity } from '$lib/api/users';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { FabXError } from '$lib/api/model/error';

	let { data }: PageProps = $props();

	let error: FabXError | null = $state(null);

	async function addPasskey() {
		error = null;

		await addWebauthnIdentity(data.me.id).catch((e) => {
			error = e;
			return '';
		});
	}
</script>

<div class="max-w-(--breakpoint-2xl)">
	<div class="container py-4">
		<div>
			<h1>Hello, {data.me.firstName} {data.me.lastName}</h1>
		</div>
		<ErrorText {error} />
		<div class="py-4">
			<Button onclick={addPasskey}>Add Passkey</Button>
		</div>
	</div>
</div>
