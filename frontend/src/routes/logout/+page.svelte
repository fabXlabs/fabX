<script lang="ts">
	import { logout } from '$lib/api/auth';

	async function doLogout() {
		const res = await logout();
		console.debug("logout", res);
	}
</script>

<div>
	<h2>Logout</h2>

	<button onclick={doLogout}>Logout</button>
</div>
