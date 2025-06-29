<script lang="ts">
	import type { AugmentedUser } from '$lib/api/model/user';
	import type { FabXError } from '$lib/api/model/error';
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table/index.js';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import type { Qualification } from '$lib/api/model/qualification';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import MemberQualificationTableActions from './MemberQualificationTableActions.svelte';
	import MemberQualificationsCardAddDropdown from './MemberQualificationsCardAddDropdown.svelte';

	interface Props {
		user: AugmentedUser;
		qualifications: Qualification[];
	}

	let { user, qualifications }: Props = $props();

	let error: FabXError | null = $state(null);
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Member Qualifications</Card.Title>
			<MemberQualificationsCardAddDropdown {user} bind:error {qualifications} />
		</div>
	</Card.Header>
	<Card.Content>
		<ErrorText {error} />
		<div class="rounded-md border">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Qualification</Table.Head>
						<Table.Head>Description</Table.Head>
						<Table.Head></Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each user.memberQualifications as qualification (qualification.id)}
						<Table.Row>
							<Table.Cell><QualificationTag {qualification} /></Table.Cell>
							<Table.Cell>{qualification.description}</Table.Cell>
							<Table.Cell class="text-right">
								<MemberQualificationTableActions
									userId={user.id}
									qualificationId={qualification.id}
									bind:error
								/>
							</Table.Cell>
						</Table.Row>
					{/each}
					{#if user.memberQualifications.length <= 0}
						<Table.Row>
							<Table.Cell colspan={3} class="text-center">No Member Qualifications</Table.Cell>
						</Table.Row>
					{/if}
				</Table.Body>
			</Table.Root>
		</div>
	</Card.Content>
</Card.Root>
