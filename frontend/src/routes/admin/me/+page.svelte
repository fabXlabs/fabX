<script lang="ts">
	import type { PageProps } from './$types';
	import { Button } from '$lib/components/ui/button';
	import { addWebauthnIdentity } from '$lib/api/users';

	let { data }: PageProps = $props();

	async function addPasskey() {
		await addWebauthnIdentity(data.me.id);
	}
</script>

<div class="max-w-screen-2xl">
	<div class="container py-4">
		<div>
			<h1>Hello, {data.me.firstName} {data.me.lastName}</h1>
		</div>
		<div class="py-4">
			<Button onclick={addPasskey}>Add Passkey</Button>
		</div>
	</div>
</div>
