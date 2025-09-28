<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Select from '$lib/components/ui/select/index.js';
	import type { PageProps } from './$types';
	import { addMemberQualification } from '$lib/api/users';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import { Label } from '$lib/components/ui/label';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { Button } from '$lib/components/ui/button';
	import Crumbs from './Crumbs.svelte';

	let { data }: PageProps = $props();

	let sortedMembers = $derived.by(() => {
		return data.users.toSorted((a, b) =>
			a.firstName.toLowerCase() < b.firstName.toLowerCase() ? -1 : 1
		);
	});

	let instructorQualifications = $derived.by(() => {
		return data.qualifications
			.filter((q) => data.me.instructorQualifications.includes(q.id))
			.toSorted((a, b) => a.orderNr - b.orderNr);
	});

	let members: string[] = $state([]);
	let qualifications: string[] = $state([]);

	const membersTriggerContent = $derived.by(() => {
		return data.users
			.filter((user) => members.includes(user.id))
			.toSorted((a, b) => (a.firstName.toLowerCase() < b.firstName.toLowerCase() ? -1 : 1));
	});

	const qualificationsTriggerContent = $derived.by(() => {
		return data.qualifications.filter((qualification) => qualifications.includes(qualification.id));
	});

	let errors: FabXError[] = $state([]);

	function resetForm() {
		members = [];
		qualifications = [];
		errors = [];
	}

	async function submit() {
		errors = [];

		const promises = members.flatMap((member) => {
			return qualifications.map((qualification) => {
				return addMemberQualification(fetch, member, qualification);
			});
		});
		for (const promise of promises) {
			await promise.catch((e) => {
				if (e.type !== 'MemberQualificationAlreadyFound') {
					errors.push(e);
				}
				return '';
			});
		}
		if (errors.length == 0) {
			resetForm();
			// TODO success notification (https://shadcn-svelte.com/docs/components/sonner)
		}
	}
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	<Crumbs />
	<h1 class="font-accent mt-4 mb-2 text-3xl">
		Welcome, {data.me.firstName}!
	</h1>
	<div class="my-6 grid gap-4">
		<Card.Root>
			<Card.Header>
				<Card.Title class="text-lg">Add Member Qualification</Card.Title>
			</Card.Header>
			<Card.Content>
				<form onsubmit={submit}>
					<div class="grid gap-4">
						<div class="grid gap-2">
							<Label for="members" class="text-muted-foreground">Members</Label>
							<Select.Root type="multiple" name="members" bind:value={members}>
								<Select.Trigger class="w-full whitespace-normal">
									<div class="flex flex-wrap justify-start">
										{#each membersTriggerContent as member (member.id)}
											<span class="inline-block flex-none">
												{member.firstName} ({member.wikiName})&nbsp;
											</span>
										{/each}
									</div>
								</Select.Trigger>
								<Select.Content>
									{#each sortedMembers as member (member.id)}
										<Select.Item value={member.id}>
											{member.firstName} ({member.wikiName})
										</Select.Item>
									{/each}
								</Select.Content>
							</Select.Root>
						</div>

						<div class="grid gap-2">
							<Label for="qualifications" class="text-muted-foreground">Qualifications</Label>
							<Select.Root type="multiple" name="qualifications" bind:value={qualifications}>
								<Select.Trigger class="w-full whitespace-normal">
									<div class="flex flex-wrap justify-start">
										{#each qualificationsTriggerContent as qualification (qualification.id)}
											<span class="inline-block flex-none">
												<QualificationTag {qualification} />
											</span>
										{/each}
									</div>
								</Select.Trigger>
								<Select.Content>
									{#each instructorQualifications as qualification (qualification.id)}
										<Select.Item value={qualification.id}>
											<QualificationTag {qualification} />
										</Select.Item>
									{/each}
								</Select.Content>
							</Select.Root>
						</div>
						<!-- eslint-disable-next-line svelte/require-each-key -->
						{#each errors as error}
							<ErrorText {error} />
						{/each}
						<Button type="submit" class="w-full">Add</Button>
					</div>
				</form>
			</Card.Content>
		</Card.Root>
	</div>
</div>
