<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Popover from '$lib/components/ui/popover/index.js';
	// noinspection ES6UnusedImports
	import * as Command from '$lib/components/ui/command/index.js';
	import type { PageProps } from './$types';
	import { addMemberQualification } from '$lib/api/users';
	import type { FabXError } from '$lib/api/model/error';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import Crumbs from './Crumbs.svelte';
	import { toast } from 'svelte-sonner';
	import { CheckIcon, CirclePlusIcon } from 'lucide-svelte';
	import { cn } from '$lib/utils';
	import ErrorText from '$lib/components/ErrorText.svelte';

	let { data }: PageProps = $props();

	let sortedMembers = $derived.by(() => {
		return data.users.toSorted((a, b) =>
			a.firstName.toLowerCase() < b.firstName.toLowerCase() ? -1 : 1
		);
	});

	let instructorQualifications = $derived.by(() => {
		return data.qualifications
			.filter((q) => data.me?.instructorQualifications?.includes(q.id) || false)
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

		for (const member of members) {
			for (const qualification of qualifications) {
				await addMemberQualification(fetch, member, qualification).catch((e) => {
					if (e.type !== 'MemberQualificationAlreadyFound') {
						errors.push(e);
					}
					return '';
				});
			}
		}

		if (errors.length == 0) {
			resetForm();
			toast.success('Added member Qualifications');
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
				<Card.Title class="text-lg">Add Member Qualification NEW</Card.Title>
			</Card.Header>
			<Card.Content class="grid gap-2">
				<Popover.Root>
					<Popover.Trigger>
						{#snippet child({ props })}
							<Button {...props} variant="outline" class="normal-case">
								<CirclePlusIcon />
								{#if members.length > 0}
									{#each membersTriggerContent as member (member.id)}
										<Badge variant="secondary">{member.firstName} ({member.wikiName})</Badge>
									{/each}
								{:else}
									Select Members
								{/if}
							</Button>
						{/snippet}
					</Popover.Trigger>
					<Popover.Content class="w-[200px] p-0" align="start">
						<Command.Root>
							<Command.Input placeholder="Member" />
							<Command.List>
								<Command.Empty>No results found.</Command.Empty>
								<Command.Group>
									{#each sortedMembers as member (member.id)}
										{@const isSelected = !!members.find((m) => m === member.id)}
										<Command.Item
											onSelect={() => {
												if (isSelected) {
													members = members.filter((m) => m !== member.id);
												} else {
													members.push(member.id);
												}
											}}
										>
											<div
												class={cn(
													'border-primary mr-2 flex size-4 items-center justify-center rounded-sm border',
													isSelected
														? 'bg-primary text-primary-foreground'
														: 'opacity-50 [&_svg]:invisible'
												)}
											>
												<CheckIcon class="size-4" />
											</div>
											<span>{member.firstName} ({member.wikiName})</span>
										</Command.Item>
									{/each}
								</Command.Group>
							</Command.List>
						</Command.Root>
					</Popover.Content>
				</Popover.Root>
				<Popover.Root>
					<Popover.Trigger>
						{#snippet child({ props })}
							<Button {...props} variant="outline" class="normal-case">
								<CirclePlusIcon />
								{#if qualifications.length > 0}
									{#each qualificationsTriggerContent as qualification (qualification.id)}
										<Badge variant="secondary">{qualification.name}</Badge>
									{/each}
								{:else}
									Select Qualifications
								{/if}
							</Button>
						{/snippet}
					</Popover.Trigger>
					<Popover.Content class="w-[200px] p-0" align="start">
						<Command.Root>
							<Command.Input placeholder="Qualification" />
							<Command.List>
								<Command.Empty>No results found.</Command.Empty>
								<Command.Group>
									{#each instructorQualifications as qualification (qualification.id)}
										{@const isSelected = !!qualifications.find((q) => q === qualification.id)}
										<Command.Item
											onSelect={() => {
												if (isSelected) {
													qualifications = qualifications.filter((q) => q !== qualification.id);
												} else {
													qualifications.push(qualification.id);
												}
											}}
										>
											<div
												class={cn(
													'border-primary mr-2 flex size-4 items-center justify-center rounded-sm border',
													isSelected
														? 'bg-primary text-primary-foreground'
														: 'opacity-50 [&_svg]:invisible'
												)}
											>
												<CheckIcon class="size-4" />
											</div>
											<span>{qualification.name}</span>
										</Command.Item>
									{/each}
								</Command.Group>
							</Command.List>
						</Command.Root>
					</Popover.Content>
				</Popover.Root>
				<!-- eslint-disable-next-line svelte/require-each-key -->
				{#each errors as error}
					<ErrorText {error} />
				{/each}
				<Button type="submit" class="w-full" onclick={submit}>Add</Button>
			</Card.Content>
		</Card.Root>
	</div>
</div>
