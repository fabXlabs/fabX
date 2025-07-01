<script lang="ts">
	import type { PageProps } from './$types';
	import PersonalInformationCard from './PersonalInformationCard.svelte';
	import LockDetailsCard from './LockDetailsCard.svelte';
	import Crumbs from './Crumbs.svelte';
	import IdentitiesCard from './IdentitiesCard.svelte';
	import QualificationsCard from './QualificationsCard.svelte';
	import {
		addInstructorQualification,
		addMemberQualification,
		removeInstructorQualification,
		removeMemberQualification
	} from '$lib/api/users';
	import DangerZoneCard from './DangerZoneCard.svelte';

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
			<IdentitiesCard user={data.augmentedUser} devices={data.devices} />
			<QualificationsCard
				qualificationType="Member Qualifications"
				accessorFunction={(user) => user.memberQualifications}
				addFunction={addMemberQualification}
				removeFunction={removeMemberQualification}
				user={data.augmentedUser}
				qualifications={data.qualifications}
			/>
			<QualificationsCard
				qualificationType="Instructor Qualifications"
				accessorFunction={(user) => user.instructorQualifications || []}
				addFunction={addInstructorQualification}
				removeFunction={removeInstructorQualification}
				user={data.augmentedUser}
				qualifications={data.qualifications}
			/>
			<DangerZoneCard user={data.augmentedUser} />
		</div>
		<!-- TODO user history -->
	{/if}
</div>
