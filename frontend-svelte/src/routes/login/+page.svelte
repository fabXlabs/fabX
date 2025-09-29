<script lang="ts">
	import { loginBasicAuth, loginWebauthn } from '$lib/api/auth';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { Separator } from '$lib/components/ui/separator';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';

	let username = $state('');
	let password = $state('');

	let showPassword = $state(false);

	let error: FabXError | null = $state(null);

	async function loginPasswordless() {
		await loginWebauthn(username)
			.then(() => {
				goto(resolve('/'));
			})
			.catch((e) => {
				error = e;
			});
	}

	async function loginUsernamePassword() {
		error = null;
		if (!showPassword) {
			showPassword = true;
		} else {
			await loginBasicAuth(username, password)
				.then(() => {
					goto(resolve('/'));
				})
				.catch((e) => {
					error = e;
				});
		}
	}
</script>

<enhanced:img
	src="/static/bg/3.jpeg"
	alt="an art installation"
	class="pointer-events-none absolute h-full w-full object-cover"
/>

<div class="flex h-screen w-full items-center justify-center px-4">
	<Card.Root class="z-50 w-72">
		<Card.Header>
			<Card.Title class="font-accent text-center text-4xl italic">fabX</Card.Title>
		</Card.Header>
		<Card.Content>
			<form>
				<div class="grid gap-4">
					<div class="grid gap-2">
						<Label for="username">Username</Label>
						<Input
							bind:value={username}
							type="text"
							id="username"
							placeholder="Username"
							autocomplete="username"
							required
						/>
					</div>
					{#if showPassword}
						<div class="grid gap-2">
							<Label for="password">Password</Label>
							<Input
								bind:value={password}
								type="password"
								id="password"
								autocomplete="current-password"
								required
							/>
						</div>
					{/if}

					<ErrorText {error} />

					{#if !showPassword}
						<Button onclick={loginPasswordless} type="submit">Login Passwordless</Button>
						<Separator />
					{/if}
					<Button
						variant="secondary"
						onclick={loginUsernamePassword}
						type={showPassword ? 'submit' : null}
					>
						Login with Password
					</Button>
				</div>
			</form>
		</Card.Content>
	</Card.Root>
</div>
