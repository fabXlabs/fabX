<script lang="ts">
	import type { FabXError } from '$lib/api/model/error';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import { Button } from '$lib/components/ui/button';
	import type { AugmentedUser } from '$lib/api/model/user';
	import type { Qualification } from '$lib/api/model/qualification';
	import { addMemberQualification } from '$lib/api/users';
	import { invalidateAll } from '$app/navigation';
	import QualificationTag from '$lib/components/QualificationTag.svelte';

	interface Props {
		user: AugmentedUser;
		error: FabXError | null;
		qualifications: Qualification[];
	}

	let { user, error = $bindable(), qualifications }: Props = $props();

	let missingMemberQualifications = $derived.by(() => {
		return qualifications.filter((qualification) => {
			return !user.memberQualifications.find((q) => q.id === qualification.id);
		});
	});

	let hasMissingMemberQualifications = $derived.by(() => {
		return missingMemberQualifications.length > 0;
	});

	async function addMemberQualification_(qualificationId: string) {
		const res = await addMemberQualification(fetch, user.id, qualificationId).catch((e) => {
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
			<Button {...props} variant="outline" disabled={!hasMissingMemberQualifications}>Add</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end">
		<DropdownMenu.Group>
			{#each missingMemberQualifications as qualification (qualification.id)}
				<DropdownMenu.Item onclick={async () => await addMemberQualification_(qualification.id)}>
					<QualificationTag {qualification} />
				</DropdownMenu.Item>
			{/each}
		</DropdownMenu.Group>
	</DropdownMenu.Content>
</DropdownMenu.Root>
