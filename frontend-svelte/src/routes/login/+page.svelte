<script lang="ts">
	import { loginBasicAuth, loginWebauthn } from '$lib/api/auth';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';

	// TODO use https://svelte.dev/docs/kit/images#sveltejs-enhanced-img
	import bgImg3 from '$lib/assets/bg/3.jpeg';
	import { Separator } from '$lib/components/ui/separator';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';

	let username = $state('');
	let password = $state('');

	let error: FabXError | null = $state(null);

	async function loginPasswordless() {
		await loginWebauthn(username)
			.then(() => {
				goto(`${base}`);
			})
			.catch((e) => {
				error = e;
			});
	}

	async function loginUsernamePassword() {
		error = null;
		await loginBasicAuth(username, password)
			.then(() => {
				goto(base || '/');
			})
			.catch((e) => {
				error = e;
			});
	}
</script>

<div class="flex h-screen w-full items-center justify-center px-4">
	<div class="absolute inset-0 bg-cover bg-center" style="background-image: url({bgImg3})"></div>
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
							autocomplete="username"
							required
						/>
					</div>

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

					<ErrorText {error} />

					<Button onclick={loginUsernamePassword} type="submit">Sign In</Button>
				</div>
			</form>

			<Separator class="my-4" />
			<div class="grid">
				<Button onclick={loginPasswordless} variant="secondary" type="submit"
					>sign in with passkey</Button
				>
			</div>
		</Card.Content>
	</Card.Root>
</div>
