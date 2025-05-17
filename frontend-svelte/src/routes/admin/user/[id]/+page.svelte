<script lang="ts">
	import type { PageProps } from './$types';
	import PersonalInformationCard from './PersonalInformationCard.svelte';
	import LockDetailsCard from './LockDetailsCard.svelte';

	let { data }: PageProps = $props();
</script>

<!-- TODO Breadcrumbs? https://next.shadcn-svelte.com/docs/components/breadcrumb -->

<div class="relative container mt-7 max-w-(--breakpoint-2xl)">
	{#if data.augmentedUser}
		<h1 class="font-accent my-2 text-3xl">
			{data.augmentedUser.firstName}
			{data.augmentedUser.lastName}
		</h1>

		<div class="my-6 grid gap-4 lg:grid-cols-2 2xl:grid-cols-3">
			<PersonalInformationCard user={data.augmentedUser} />
			<LockDetailsCard user={data.augmentedUser} />
		</div>

		<!-- TODO Identities -->
		<!-- TODO Instructor Qualifications -->
		<!-- TODO Instructor Qualifications -->
		<!-- TODO Admin or not -->
		<!-- TODO deleting User -->
	{/if}
</div>
