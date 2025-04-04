<script lang="ts">
	import { goto } from '$app/navigation';
	import { logout } from '$lib/api/auth';

	async function doLogout() {
		const res = await logout();
		console.debug("logout", res);
		goto('/');
	}
</script>

<div>
	<h2>Logout</h2>

	<button onclick={doLogout}>Logout</button>
</div>
