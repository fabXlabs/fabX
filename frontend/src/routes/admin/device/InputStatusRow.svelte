<script lang="ts">
	import type { AugmentedInputDescription } from '$lib/api/model/device';
	import { CircleQuestionMark, ToggleLeft, ToggleRight } from 'lucide-svelte';

	interface Props {
		pin: string;
		inputDescription: AugmentedInputDescription;
	}

	let { pin, inputDescription }: Props = $props();
</script>

<tr>
	<td class="px-1 text-right">{pin}: </td>
	{#if inputDescription.inputPinStatus === 'INPUT_HIGH'}
		<td class="px-1"><ToggleRight class="inline" color={inputDescription.colourHigh} /></td>
		<td class="px-1">{inputDescription.name} ({inputDescription.descriptionHigh})</td>
	{:else if inputDescription.inputPinStatus === 'INPUT_LOW'}
		<td class="px-1"><ToggleLeft class="inline" color={inputDescription.colourLow} /></td>
		<td class="px-1">{inputDescription.name} ({inputDescription.descriptionLow})</td>
	{:else}
		<td class="px-1"><CircleQuestionMark class="inline" /></td>
		<td class="px-1">{inputDescription.name}</td>
	{/if}
</tr>
