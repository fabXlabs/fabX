<script lang="ts">
	import type { PageProps } from './$types';
	import Crumbs from './Crumbs.svelte';
	import QualificationDetailsCard from './QualificationDetailsCard.svelte';
	import DangerZoneCard from './DangerZoneCard.svelte';

	let { data }: PageProps = $props();
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.qualification}
		<Crumbs qualification={data.qualification} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.qualification.name}
		</h1>
		<div>
			<div class="my-6 grid gap-4">
				<QualificationDetailsCard qualification={data.qualification} />
				<DangerZoneCard qualification={data.qualification} />
			</div>
		</div>
	{/if}
</div>
