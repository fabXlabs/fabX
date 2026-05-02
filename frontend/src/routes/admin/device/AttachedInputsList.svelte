<script lang="ts">
	import type { AugmentedInputDescription } from '$lib/api/model/device';
	import InputStatusRow from './InputStatusRow.svelte';

	interface Props {
		attachedInputs: Record<number, AugmentedInputDescription>;
		pinStatusUpdatedAt: string | null;
	}

	let { attachedInputs, pinStatusUpdatedAt }: Props = $props();
</script>

<table class="border-spacing-1">
	<tbody>
		{#each Object.entries(attachedInputs) as [pin, inputDescription] (pin)}
			<InputStatusRow {pin} {inputDescription} />
		{/each}
		{#if pinStatusUpdatedAt}
			<tr>
				<td colspan="3" class="text-center" style="font-size: 6pt;">
					{pinStatusUpdatedAt}
				</td>
			</tr>
		{/if}
	</tbody>
</table>
