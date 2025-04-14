<script lang="ts">
	import type { PageProps } from './$types';
	import QualificationTag from '$lib/components/QualificationTag.svelte';

	let { data }: PageProps = $props();
</script>

<div class="max-w-(--breakpoint-2xl)">
	<div class="container items-center py-4">
		{#if data.augmentedUser}
			<h1>User {data.augmentedUser.firstName} {data.augmentedUser.lastName}</h1>
			<h2>Member Qualifications</h2>
			<div>
				{#each data.augmentedUser.memberQualifications as qualification (qualification.id)}
					<QualificationTag {qualification} />
				{/each}
			</div>
			<h2>Instructor Qualifications</h2>
			<div>
				{#each data.augmentedUser.instructorQualifications || [] as qualification (qualification.id)}
					<QualificationTag {qualification} />
				{/each}
			</div>
		{/if}
	</div>
</div>
