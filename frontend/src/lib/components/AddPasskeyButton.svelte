<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { addWebauthnIdentity } from '$lib/api/users';
	import type { FabXError } from '$lib/api/model/error';
	import type { User } from '$lib/api/model/user';

	interface Props {
		me: User;
		error: FabXError | null;
	}

	let { me, error = $bindable() }: Props = $props();

	async function addPasskey() {
		error = null;

		await addWebauthnIdentity(fetch, me.id).catch((e) => {
			error = e;
			return '';
		});
	}
</script>

<Button onclick={addPasskey}>Add Passkey</Button>
