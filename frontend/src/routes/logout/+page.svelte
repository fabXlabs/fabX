<script lang="ts">
	import { logout } from '$lib/auth';

	async function doLogout() {
		const res = await logout();
		console.debug("logout", res);
	}
</script>

<div class="prose">
	<h2>Logout</h2>

	<button onclick={doLogout}>Logout</button>
</div>
