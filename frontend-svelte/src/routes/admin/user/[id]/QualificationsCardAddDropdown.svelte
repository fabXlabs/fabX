<script lang="ts">
	import type { FabXError } from '$lib/api/model/error';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import { Button } from '$lib/components/ui/button';
	import type { AugmentedUser } from '$lib/api/model/user';
	import type { Qualification } from '$lib/api/model/qualification';
	import { invalidateAll } from '$app/navigation';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import type { FetchFunction } from '$lib/api';

	interface Props {
		user: AugmentedUser;
		accessorFunction: (user: AugmentedUser) => Qualification[];
		addFunction: (fetch: FetchFunction, userId: string, qualificationId: string) => Promise<string>;
		error: FabXError | null;
		qualificationAdditionFilterFunction: (qualificationId: string) => boolean;
		qualifications: Qualification[];
	}

	let {
		user,
		addFunction,
		accessorFunction,
		error = $bindable(),
		qualificationAdditionFilterFunction,
		qualifications
	}: Props = $props();

	let missingQualifications = $derived.by(() => {
		return qualifications
			.filter((qualification) => {
				return !accessorFunction(user).find((q) => q.id === qualification.id);
			})
			.filter((qualification) => qualificationAdditionFilterFunction(qualification.id));
	});

	let hasMissingQualifications = $derived.by(() => {
		return missingQualifications.length > 0;
	});

	async function addQualification_(qualificationId: string) {
		const res = await addFunction(fetch, user.id, qualificationId).catch((e) => {
			error = e;
			return '';
		});
		if (res) {
			await invalidateAll();
		}
		return res;
	}
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="outline" disabled={!hasMissingQualifications}>Add</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end">
		<DropdownMenu.Group>
			{#each missingQualifications as qualification (qualification.id)}
				<DropdownMenu.Item onclick={async () => await addQualification_(qualification.id)}>
					<QualificationTag {qualification} />
				</DropdownMenu.Item>
			{/each}
		</DropdownMenu.Group>
	</DropdownMenu.Content>
</DropdownMenu.Root>
