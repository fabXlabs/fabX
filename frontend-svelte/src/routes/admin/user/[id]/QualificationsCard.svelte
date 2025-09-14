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
	import QualificationTableActions from './QualificationTableActions.svelte';
	import QualificationsCardAddDropdown from './QualificationsCardAddDropdown.svelte';
	import type { FetchFunction } from '$lib/api';

	interface Props {
		qualificationType: string;
		user: AugmentedUser;
		accessorFunction: (user: AugmentedUser) => Qualification[];
		addFunction: (fetch: FetchFunction, userId: string, qualificationId: string) => Promise<string>;
		removeFunction: (
			fetch: FetchFunction,
			userId: string,
			qualificationId: string
		) => Promise<string>;
		qualificationAdditionFilterFunction: (qualificationId: string) => boolean;
		qualifications: Qualification[];
	}

	let {
		qualificationType,
		user,
		accessorFunction,
		addFunction,
		removeFunction,
		qualificationAdditionFilterFunction,
		qualifications
	}: Props = $props();

	let error: FabXError | null = $state(null);

	let existingQualification: Qualification[] = $derived.by(() => {
		return accessorFunction(user);
	});

	let hasExistingQualifications: boolean = $derived(existingQualification.length > 0);
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">{qualificationType}</Card.Title>
			<QualificationsCardAddDropdown
				{user}
				{addFunction}
				{accessorFunction}
				bind:error
				{qualificationAdditionFilterFunction}
				{qualifications}
			/>
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
					{#each existingQualification as qualification (qualification.id)}
						<Table.Row>
							<Table.Cell>
								<QualificationTag {qualification} />
							</Table.Cell>
							<Table.Cell>{qualification.description}</Table.Cell>
							<Table.Cell class="text-right">
								<QualificationTableActions
									{removeFunction}
									userId={user.id}
									qualificationId={qualification.id}
									bind:error
								/>
							</Table.Cell>
						</Table.Row>
					{/each}
					{#if !hasExistingQualifications}
						<Table.Row>
							<Table.Cell colspan={3} class="text-center">No {qualificationType}</Table.Cell>
						</Table.Row>
					{/if}
				</Table.Body>
			</Table.Root>
		</div>
	</Card.Content>
</Card.Root>
