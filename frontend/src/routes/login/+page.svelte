<script lang="ts">
	import { loginBasicAuth } from '$lib/auth';

	let username = $state('');
	let password = $state('');

	let error = $state('');

	let loggedIn = $state(false);

	async function login() {
		error = '';
		await loginBasicAuth(username, password)
			.then(() => {
				username = '';
				password = '';
				loggedIn = true;
			})
			.catch(e => {
				error = JSON.stringify(e);
			});
	}
</script>

<div class="prose">
	<h1>Login</h1>

	<form>
		<label>
			Username:
			<input bind:value="{username}" type="text" name="username" autocomplete="username" required />
		</label>

		<div>
			<label>
				Password:
				<input bind:value="{password}" type="password" name="password" autocomplete="current-password" required />
			</label>
		</div>

		<button onclick={login} type="submit">Login</button>
	</form>

	{#if loggedIn}
		<div>
			<h2>Logged in!</h2>
			<a href="/qualification">Qualifications</a>
		</div>
	{/if}

	{#if error}
		<div>Error: {error}</div>
	{/if}
</div>
