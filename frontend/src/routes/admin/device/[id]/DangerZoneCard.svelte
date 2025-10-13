<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import DeleteDeviceAlert from './DeleteDeviceAlert.svelte';
	import RestartDeviceAlert from './RestartDeviceAlert.svelte';
	import UpdateDeviceFirmwareAlert from './UpdateDeviceFirmwareAlert.svelte';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<Card.Title class="text-lg">Danger Zone</Card.Title>
	</Card.Header>
	<Card.Content class="flex flex-col gap-2">
		<RestartDeviceAlert {device} />
		<UpdateDeviceFirmwareAlert {device} />
		<DeleteDeviceAlert {device} />
	</Card.Content>
</Card.Root>
