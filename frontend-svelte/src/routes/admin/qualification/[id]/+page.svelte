<script lang="ts">
	import type { PageProps } from './$types';
	import QualificationTag from '$lib/components/QualificationTag.svelte';

	let { data }: PageProps = $props();
</script>

<div class="max-w-screen-2xl">
	<div class="container items-center py-4">
		{#if data.qualification}
			<h1>Qualification {data.qualification.name}</h1>
			<div>
				<QualificationTag qualification={data.qualification} />
			</div>
		{/if}
	</div>
</div>
