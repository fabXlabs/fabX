<script lang="ts">
	import { resolve } from '$app/paths';
	import type { Device } from '$lib/api/model/device';
	import type { DeviceActorId, SystemActorId, User, UserActorId } from '$lib/api/model/user';

	interface Props {
		actor: SystemActorId | Device | DeviceActorId | User | UserActorId;
	}

	let { actor }: Props = $props();
</script>

{#if 'type' in actor}
	{#if actor.type === 'cloud.fabX.fabXaccess.common.model.SystemActorId'}
		<span>SYSTEM</span>
	{:else if actor.type === 'cloud.fabX.fabXaccess.common.model.DeviceId'}
		<span>Device {actor.value}</span>
	{:else if actor.type === 'cloud.fabX.fabXaccess.common.model.UserId'}
		<span>User {actor.value}</span>
	{:else}
		<span>Unknown {JSON.stringify(actor)}</span>
	{/if}
{:else if 'firstName' in actor}
	<a href={resolve('/admin/user/[id]', { id: actor.id })}
		>{actor.firstName} {actor.lastName} ({actor.wikiName})</a
	>
{:else if 'attachedTools' in actor}
	<span>{actor.name}</span>
{:else}
	<span>Unknown {JSON.stringify(actor)}</span>
{/if}
