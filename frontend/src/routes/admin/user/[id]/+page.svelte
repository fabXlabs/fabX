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
	import UserSourcingEventsCard from './UserSourcingEventsCard.svelte';

	let { data }: PageProps = $props();

	function memberQualificationAdditionFilterFunction(qualificationId: string): boolean {
		const instructorQualifications = data.me.instructorQualifications || [];
		return instructorQualifications.includes(qualificationId);
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	function instructorQualificationAdditionFilterFunction(qualificationId: string): boolean {
		// admin can hand out any instructor qualification
		return true;
	}
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
				user={data.augmentedUser}
				accessorFunction={(user) => user.memberQualifications}
				addFunction={addMemberQualification}
				removeFunction={removeMemberQualification}
				qualificationAdditionFilterFunction={memberQualificationAdditionFilterFunction}
				qualifications={data.qualifications}
			/>
			<QualificationsCard
				qualificationType="Instructor Qualifications"
				user={data.augmentedUser}
				accessorFunction={(user) => user.instructorQualifications || []}
				addFunction={addInstructorQualification}
				removeFunction={removeInstructorQualification}
				qualificationAdditionFilterFunction={instructorQualificationAdditionFilterFunction}
				qualifications={data.qualifications}
			/>
			<DangerZoneCard user={data.augmentedUser} />
			<UserSourcingEventsCard
				events={data.sourcingEvents || []}
				users={data.users}
				devices={data.devices}
				qualifications={data.qualifications}
			/>
		</div>
	{/if}
</div>
