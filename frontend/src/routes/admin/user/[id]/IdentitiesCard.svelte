<script lang="ts">
	import type {
		AugmentedUser,
		CardIdentity,
		PhoneNrIdentity,
		UserIdentity,
		WebauthnIdentity
	} from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table/index.js';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import IdentitiesCardTableActions from './IdentitiesCardTableActions.svelte';
	import IdentitiesCardAddDropdown from './IdentitiesCardAddDropdown.svelte';
	import type { Device } from '$lib/api/model/device';
	import { toHexString } from '$lib/utils';

	interface Props {
		user: AugmentedUser;
		devices: Device[];
	}

	let { user, devices }: Props = $props();

	let identityTypeOrder = [
		'cloud.fabX.fabXaccess.user.rest.PinIdentity',
		'cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity',
		'cloud.fabX.fabXaccess.user.rest.CardIdentity',
		'cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity',
		'cloud.fabX.fabXaccess.user.rest.WebauthnIdentity'
	];

	let sortedIdentities = $derived.by(() => {
		return user.identities.toSorted((a, b) => {
			const res = identityTypeOrder.indexOf(a.type) - identityTypeOrder.indexOf(b.type);

			if (res == 0) {
				switch (a.type) {
					case 'cloud.fabX.fabXaccess.user.rest.CardIdentity':
						return a.cardId < (b as CardIdentity).cardId ? -1 : 1;
					case 'cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity':
						return a.phoneNr < (b as PhoneNrIdentity).phoneNr ? -1 : 1;
					case 'cloud.fabX.fabXaccess.user.rest.WebauthnIdentity':
						return a.credentialId < (b as WebauthnIdentity).credentialId ? -1 : 1;
					default:
						return 0;
				}
			} else {
				return res;
			}
		});
	});

	let error: FabXError | null = $state(null);

	function identityDisplayType(identity: UserIdentity): string {
		switch (identity.type) {
			case 'cloud.fabX.fabXaccess.user.rest.PinIdentity':
				return 'Pin';
			case 'cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity':
				return 'Username/Password';
			case 'cloud.fabX.fabXaccess.user.rest.CardIdentity':
				return 'Card';
			case 'cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity':
				return 'Phone Nr.';
			case 'cloud.fabX.fabXaccess.user.rest.WebauthnIdentity':
				return 'Webauthn';
		}
	}

	function identityId(identity: UserIdentity): string {
		switch (identity.type) {
			case 'cloud.fabX.fabXaccess.user.rest.PinIdentity':
				return '****';
			case 'cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity':
				return identity.username;
			case 'cloud.fabX.fabXaccess.user.rest.CardIdentity':
				return identity.cardId;
			case 'cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity':
				return identity.phoneNr;
			case 'cloud.fabX.fabXaccess.user.rest.WebauthnIdentity':
				return toHexString(identity.credentialId);
		}
	}
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Identities</Card.Title>
			<IdentitiesCardAddDropdown {user} bind:error {devices} />
		</div>
	</Card.Header>
	<Card.Content>
		<ErrorText {error} />
		<div class="rounded-md border">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Type</Table.Head>
						<Table.Head>Identity</Table.Head>
						<Table.Head></Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					<!-- eslint-disable-next-line svelte/require-each-key -->
					{#each sortedIdentities as identity}
						<Table.Row>
							<Table.Cell>{identityDisplayType(identity)}</Table.Cell>
							<Table.Cell>{identityId(identity)}</Table.Cell>
							<Table.Cell class="text-right"
								><IdentitiesCardTableActions userId={user.id} {identity} bind:error /></Table.Cell
							>
						</Table.Row>
					{/each}
					{#if sortedIdentities.length <= 0}
						<Table.Row>
							<Table.Cell colspan={3} class="text-center">No Identities</Table.Cell>
						</Table.Row>
					{/if}
				</Table.Body>
			</Table.Root>
		</div>
	</Card.Content>
</Card.Root>
