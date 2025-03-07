<script lang="ts">
	import type { Qualification } from '$lib/api/model/qualification';
	import QualificationTag from '$lib/components/QualificationTag.svelte';

	interface Props {
		qualifications: Qualification[];
	}

	let { qualifications }: Props = $props();
</script>

<div class="flex flex-wrap space-y-1">
	{#each qualifications as qualification}
		<QualificationTag {qualification} />
	{/each}
</div>
