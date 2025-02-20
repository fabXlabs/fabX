<script lang="ts">
	import { loginBasicAuth } from '$lib/auth';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { goto } from '$app/navigation';

	let username = $state('');
	let password = $state('');

	let error = $state('');

	async function login() {
		error = '';
		await loginBasicAuth(username, password)
			.then(() => {
				goto('/qualification');
			})
			.catch(e => {
				error = JSON.stringify(e);
			});
	}
</script>

<div class="flex h-screen w-full items-center justify-center px-4">
	<Card.Root class="min-w-72">
		<Card.Header>
			<Card.Title class="text-2xl">fabX Login</Card.Title>
		</Card.Header>
		<Card.Content>
			<form>
				<div class="grid gap-4">
					<div class="grid gap-2">
						<Label for="username">Username</Label>
						<Input bind:value="{username}" type="text" id="username" placeholder="Username" autocomplete="username"
									 required />
					</div>
					<div class="grid gap-2">
						<Label for="password">Password</Label>
						<Input bind:value="{password}" type="password" id="password" autocomplete="current-password"
									 required />
					</div>

					{#if error}
						<div>
							<p class="text-sm text-red-600">Error: {error}</p>
						</div>
					{/if}

					<Button onclick={login} type="submit">Login</Button>
				</div>
			</form>
		</Card.Content>
	</Card.Root>
</div>
