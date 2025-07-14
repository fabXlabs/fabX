<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type {
		AugmentedUserSourcingEvent,
		DeviceActorId,
		SystemActorId,
		User,
		UserActorId,
		UserSourcingEvent
	} from '$lib/api/model/user';
	import HiddenPin from './HiddenPin.svelte';
	import type { Qualification } from '$lib/api/model/qualification';
	import type { Device } from '$lib/api/model/device';
	import Actor from './Actor.svelte';
	import QualificationTag from '$lib/components/QualificationTag.svelte';

	interface Props {
		events: UserSourcingEvent[];
		users: User[];
		devices: Device[];
		qualifications: Qualification[];
	}

	let { events, users, devices, qualifications }: Props = $props();

	let eventsAugmentedAndReversed = $derived.by(() => {
		return events.toReversed().map(augment);
	});

	function augment(event: UserSourcingEvent): AugmentedUserSourcingEvent {
		return {
			...event,
			type: formatType(event),
			timestamp: formatTimestamp(event),
			actor: resolveActor(event),
			qualification: resolveQualification(event)
		};
	}

	function formatType(event: UserSourcingEvent): string {
		let type = event.type.split('.').pop() || '';
		return type.replace(/([A-Z])/g, ' $1');
	}

	function formatTimestamp(event: UserSourcingEvent): string {
		let ts = new Date(Date.parse(event.timestamp));
		return ts.toISOString().replace(/T/, ' ').slice(0, 16);
	}

	function resolveActor(
		event: UserSourcingEvent
	): SystemActorId | Device | DeviceActorId | User | UserActorId {
		switch (event.actorId.type) {
			case 'cloud.fabX.fabXaccess.common.model.SystemActorId':
				return event.actorId;
			case 'cloud.fabX.fabXaccess.common.model.UserId':
				return (
					users.find((user) => user.id === (event.actorId as UserActorId).value) || event.actorId
				);
			case 'cloud.fabX.fabXaccess.common.model.DeviceId':
				return (
					devices.find((device) => device.id === (event.actorId as DeviceActorId).value) ||
					event.actorId
				);
		}
	}

	function resolveQualification(event: UserSourcingEvent): Qualification | undefined {
		if ('qualificationId' in event) {
			return qualifications.find((qualification) => qualification.id === event.qualificationId);
		} else {
			return undefined;
		}
	}

	function reduceEvent(event: AugmentedUserSourcingEvent): string[][] {
		const reducedEvent = {
			...event,
			type: undefined,
			aggregateRootId: undefined,
			aggregateVersion: undefined,
			actor: undefined,
			actorId: undefined,
			correlationId: undefined,
			timestamp: undefined,
			authenticator: undefined,
			hash: undefined,
			pin: undefined,
			cardSecret: undefined,
			qualificationId: undefined
		};

		return Object.entries(reducedEvent)
			.filter((entry) => entry[1])
			.map((entry) => [entry[0], newValueToString(entry[1] as object)])
			.filter((entry) => entry[1]);
	}

	function newValueToString(value: object): string {
		if (typeof value === 'object' && 'type' in value) {
			if (value.type == 'cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs') {
				return '';
			} else if (
				(value.type as string).startsWith(
					'cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue'
				) &&
				'value' in value
			) {
				return value.value as string;
			}
		}

		return JSON.stringify(value);
	}

	function extractPin(event: AugmentedUserSourcingEvent): string | null {
		if ('pin' in event) {
			return event.pin as string;
		} else {
			return null;
		}
	}
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<Card.Title class="text-lg">History</Card.Title>
	</Card.Header>
	<Card.Content class="grid grid-cols-[1fr_2fr] gap-3">
		{#each eventsAugmentedAndReversed as event (event.aggregateVersion)}
			<div>
				<div class="text-right text-lg">v{event.aggregateVersion || '1'}</div>
				<div class="text-right text-sm">{event.timestamp}</div>
			</div>
			<div>
				<div class="text-lg">{event.type}</div>
				<div class="text-sm">by <Actor actor={event.actor} /></div>
				{#if extractPin(event)}
					<div class="text-sm">PIN: <HiddenPin pin={extractPin(event) || ''} /></div>
				{/if}
				{#if event.qualification}
					<div class="text-sm">
						Qualification: <QualificationTag qualification={event.qualification} />
					</div>
				{/if}
				{#each reduceEvent(event) as entry (entry[0])}
					<div class="text-sm">{entry[0]}: {entry[1]}</div>
				{/each}
			</div>
		{/each}
	</Card.Content>
</Card.Root>
