<script lang="ts">
	import type { Device } from '$lib/api/model/device';

	interface Props {
		devices: Device[];
	}

	let { devices }: Props = $props();
</script>

<table>
	<thead>
	<tr>
		<th>Name</th>
		<th>Attached Tools</th>
		<th>Firmware</th>
	</tr>
	</thead>
	<tbody>
	{#each devices as device}
		<tr>
			<td>{device.name}</td>
			<td>{JSON.stringify(Object.values(device.attachedTools))}</td>
			<td>{device.actualFirmwareVersion}</td>
		</tr>
	{/each}
	</tbody>
</table>
