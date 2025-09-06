<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	// noinspection ES6UnusedImports
	import * as Select from '$lib/components/ui/select/index.js';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { addTool } from '$lib/api/tools';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import { Plus } from 'lucide-svelte';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Switch } from '$lib/components/ui/switch';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import type { Qualification } from '$lib/api/model/qualification';

	interface Props {
		qualifications: Qualification[];
	}

	let { qualifications }: Props = $props();

	let sheetOpen = $state(false);

	let name = $state('');
	let type = $state('UNLOCK');
	let requires2FA = $state(false);
	let time = $state(0);
	let idleState = $state('IDLE_LOW');
	let wikiLink = $state('');
	let requiredQualifications: string[] = $state([]);

	let error: FabXError | null = $state(null);

	const typeTriggerContent = $derived.by(() => {
		if (type === 'UNLOCK') {
			return 'Unlock';
		} else if (type === 'KEEP') {
			return 'Keep';
		} else {
			console.error('Unknown tool type', type);
			return '?';
		}
	});

	const idleStateTriggerContent = $derived.by(() => {
		if (idleState === 'IDLE_HIGH') {
			return 'Idle High';
		} else if (idleState === 'IDLE_LOW') {
			return 'Idle Low';
		} else {
			console.error('Unknown idle state', idleState);
			return '?';
		}
	});

	const requiredQualificationsTriggerContent = $derived.by(() => {
		return qualifications.filter((qualification) =>
			requiredQualifications.includes(qualification.id)
		);
	});

	async function submit() {
		error = null;

		const res = await addTool(fetch, {
			name,
			type,
			requires2FA,
			time,
			idleState,
			wikiLink,
			requiredQualifications
		}).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			sheetOpen = false;
			await goto(`${base}/admin/tool/${res}`);
		}
	}
</script>

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Trigger class={buttonVariants({ variant: 'normalcase' })}>
		<Plus />
		Add Tool
	</Sheet.Trigger>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Add Tool</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="name">Name</Label>
					<Input id="name" bind:value={name} />
				</div>
				<div class="grid gap-2">
					<Label for="type" class="text-muted-foreground">Type</Label>
					<Select.Root type="single" name="type" bind:value={type}>
						<Select.Trigger
							class="w-full disabled:border-transparent disabled:opacity-100 disabled:shadow-none"
						>
							{typeTriggerContent}
						</Select.Trigger>
						<Select.Content>
							<Select.Item value="UNLOCK">Unlock</Select.Item>
							<Select.Item value="KEEP">Keep</Select.Item>
						</Select.Content>
					</Select.Root>
				</div>
				<div class="grid gap-2">
					<Label for="requires2FA" class="text-muted-foreground">Requires 2FA</Label>
					<Switch id="requires2FA" bind:checked={requires2FA} />
				</div>
				<div class="grid gap-2">
					<Label for="time" class="text-muted-foreground">Time</Label>
					<Input
						id="time"
						inputmode="numeric"
						pattern={'\\d{1,10}'}
						min="0"
						class="disabled:border-transparent disabled:opacity-100"
						bind:value={time}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="idleState" class="text-muted-foreground">Idle State</Label>
					<Select.Root type="single" name="idleState" bind:value={idleState}>
						<Select.Trigger
							class="w-full disabled:border-transparent disabled:opacity-100 disabled:shadow-none"
						>
							{idleStateTriggerContent}
						</Select.Trigger>
						<Select.Content>
							<Select.Item value="IDLE_LOW">Idle Low</Select.Item>
							<Select.Item value="IDLE_HIGH">Idle High</Select.Item>
						</Select.Content>
					</Select.Root>
				</div>
				<div class="grid gap-2">
					<Label for="wikiLink">Wiki Link</Label>
					<Input id="wikiLink" type="url" bind:value={wikiLink} />
				</div>
				<div class="grid gap-2">
					<Label for="requiredQualifications" class="text-muted-foreground"
						>Required Qualifications</Label
					>
					<Select.Root
						type="multiple"
						name="requiredQualifications"
						bind:value={requiredQualifications}
					>
						<Select.Trigger
							class="w-full whitespace-normal disabled:border-transparent disabled:opacity-100 disabled:shadow-none"
						>
							<div class="flex flex-wrap justify-start">
								{#each requiredQualificationsTriggerContent as qualification (qualification.id)}
									<span class="inline-block flex-none">
										<QualificationTag {qualification} />
									</span>
								{/each}
							</div>
						</Select.Trigger>
						<Select.Content>
							{#each qualifications as qualification (qualification.id)}
								<Select.Item value={qualification.id}>
									<QualificationTag {qualification} />
								</Select.Item>
							{/each}
						</Select.Content>
					</Select.Root>
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Add</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
