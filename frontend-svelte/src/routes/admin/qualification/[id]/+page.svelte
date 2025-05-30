<script lang="ts">
	import type { PageProps } from './$types';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.qualification}
		<Crumbs qualification={data.qualification} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.qualification.name}
		</h1>
		<div>
			<QualificationTag qualification={data.qualification} />
		</div>
	{/if}
</div>
