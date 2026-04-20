<script lang="ts">
	import type { PinStatus } from '$lib/api/model/device';

	interface Props {
		pinStatus: PinStatus | null;
	}

	let { pinStatus }: Props = $props();

	const backupRange = Array.from(Array(8).keys());
</script>

<div class="relative">
	{#if pinStatus}
		<table class="pinStatus text-xs">
			<thead>
				<tr>
					{#each pinStatus.inputPinStatus.entries() as e (e[0])}
						<th class="p-0.5">{e[0]}</th>
					{/each}
				</tr>
			</thead>
			<tbody>
				<tr>
					{#each pinStatus.inputPinStatus.entries() as e (e[0])}
						{#if e[1] === 'INPUT_HIGH'}
							<td class="pinHigh p-0.5">1</td>
						{:else if e[1] === 'INPUT_LOW'}
							<td class="pinLow p-0.5">0</td>
						{:else}
							<td class="p-0.5">?</td>
						{/if}
					{/each}
				</tr>
				<tr>
					<td colspan={pinStatus.inputPinStatus.size} class="text-center" style="font-size: 6pt;">
						{pinStatus.updatedAt}
					</td>
				</tr>
			</tbody>
		</table>
	{:else}
		<table class="pinStatus relative border-0! text-xs">
			<thead>
				<tr>
					{#each backupRange as e (e)}
						<th class="p-0.5">{e}</th>
					{/each}
				</tr>
			</thead>
			<tbody>
				<tr>
					{#each backupRange as e (e)}
						<td class="pinUnknown p-0.5">?</td>
					{/each}
				</tr>
				<tr>
					<td colspan={backupRange.length} class="text-center" style="font-size: 6pt;">
						unknown
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan={backupRange.length}>
						<div
							class="overlay absolute top-0 right-0 bottom-0 left-0 z-10 flex items-center justify-center text-lg"
						>
							?
						</div>
					</td>
				</tr>
			</tfoot>
		</table>
	{/if}
</div>

<style>
	.pinStatus {
		font-family: monospace;
	}

	table.pinStatus {
		border-collapse: collapse;
		border: 1px solid hsl(var(--foreground));
	}

	.pinStatus th,
	td {
		border: 1px solid hsl(var(--foreground));
	}

	td.pinLow {
		background-color: #050;
	}

	td.pinHigh {
		background-color: #0e0;
	}

	.overlay {
		background-color: hsl(var(--background));
	}
</style>
