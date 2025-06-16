<script lang="ts">
	import type { PageProps } from './$types';
	import PersonalInformationCard from './PersonalInformationCard.svelte';
	import LockDetailsCard from './LockDetailsCard.svelte';
	import Crumbs from './Crumbs.svelte';
	import IdentitiesCard from './IdentitiesCard.svelte';

	let { data }: PageProps = $props();
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.augmentedUser}
		<Crumbs user={data.augmentedUser} />
		<h1 class="font-accent mt-4 mb-2 text-3xl">
			{data.augmentedUser.firstName}
			{data.augmentedUser.lastName}
		</h1>

		<div class="my-6 grid gap-4 lg:grid-cols-2 2xl:grid-cols-3">
			<PersonalInformationCard user={data.augmentedUser} />
			<LockDetailsCard user={data.augmentedUser} />
			<IdentitiesCard user={data.augmentedUser} />
		</div>

		<!-- TODO Member Qualifications -->
		<!-- TODO Instructor Qualifications -->
		<!-- TODO Admin or not -->
		<!-- TODO deleting User -->
	{/if}
</div>
