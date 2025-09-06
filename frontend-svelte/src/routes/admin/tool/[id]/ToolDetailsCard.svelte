<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Select from '$lib/components/ui/select/index.js';
	import { Switch } from '$lib/components/ui/switch/index.js';
	import type { AugmentedTool, ToolDetails } from '$lib/api/model/tool';
	import type { Qualification } from '$lib/api/model/qualification';
	import type { FabXError } from '$lib/api/model/error';
	import { changeToolDetails } from '$lib/api/tools';
	import { invalidateAll } from '$app/navigation';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import QualificationTag from '$lib/components/QualificationTag.svelte';
	import { areSetsEqual } from '$lib/utils';

	interface Props {
		tool: AugmentedTool;
		qualifications: Qualification[];
	}

	let { tool, qualifications }: Props = $props();

	let editing = $state(false);

	let name = $state(tool.name);
	let type = $state(tool.type);
	let requires2FA = $state(tool.requires2FA);
	let time = $state(tool.time);
	let idleState = $state(tool.idleState);
	let enabled = $state(tool.enabled);
	let notes = $state(tool.notes || '');
	let wikiLink = $state(tool.wikiLink);
	let requiredQualifications = $state(tool.requiredQualifications.map((q) => q.id));

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

	let error: FabXError | null = $state(null);

	function resetForm() {
		name = tool.name;
		type = tool.type;
		requires2FA = tool.requires2FA;
		time = tool.time;
		idleState = tool.idleState;
		enabled = tool.enabled;
		notes = tool.notes || '';
		wikiLink = tool.wikiLink;
		requiredQualifications = tool.requiredQualifications.map((q) => q.id);
		error = null;
	}

	function toggleEditing() {
		editing = !editing;
		if (!editing) {
			resetForm();
		}
	}

	async function submit() {
		const notes_ = notes ? notes : null;

		const originalRequiredQualificationIds = new Set(tool.requiredQualifications.map((q) => q.id));
		const newRequiredQualificationIds = new Set(requiredQualifications);

		console.debug('originalRequiredQualificationIds', originalRequiredQualificationIds);
		console.debug('newRequiredQualificationIds', newRequiredQualificationIds);

		const details: ToolDetails = {
			name: name != tool.name ? { newValue: name } : null,
			type: type != tool.type ? { newValue: type } : null,
			requires2FA: requires2FA != tool.requires2FA ? { newValue: requires2FA } : null,
			time: time != tool.time ? { newValue: time } : null,
			idleState: idleState != tool.idleState ? { newValue: idleState } : null,
			enabled: enabled != tool.enabled ? { newValue: enabled } : null,
			notes: notes_ != tool.notes ? { newValue: notes_ } : null,
			wikiLink: wikiLink != tool.wikiLink ? { newValue: wikiLink } : null,
			requiredQualifications: !areSetsEqual(
				originalRequiredQualificationIds,
				newRequiredQualificationIds
			)
				? { newValue: requiredQualifications }
				: null
		};

		const res = await changeToolDetails(fetch, tool.id, details).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			editing = false;
			await invalidateAll();
		}
	}
</script>

<Card.Root>
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Tool Details</Card.Title>
			<Button variant="outline" onclick={toggleEditing}>
				{#if !editing}
					Edit
				{:else}
					Cancel
				{/if}
			</Button>
		</div>
	</Card.Header>
	<Card.Content>
		<form onsubmit={submit}>
			<div class="grid gap-4">
				<div class="grid gap-2">
					<Label for="name" class="text-muted-foreground">Name</Label>
					<Input
						id="name"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={name}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="type" class="text-muted-foreground">Type</Label>
					<Select.Root type="single" name="type" bind:value={type} disabled={!editing}>
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
					<!-- TODO fix alignment -->
					<Switch id="requires2FA" bind:checked={requires2FA} disabled={!editing} />
				</div>
				<div class="grid gap-2">
					<Label for="time" class="text-muted-foreground">Time</Label>
					<Input
						id="time"
						inputmode="numeric"
						pattern={'\\d{1,10}'}
						min="0"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						bind:value={time}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="idleState" class="text-muted-foreground">Idle State</Label>
					<Select.Root type="single" name="idleState" bind:value={idleState} disabled={!editing}>
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
					<Label for="enabled" class="text-muted-foreground">Enabled</Label>
					<Switch id="enabled" bind:checked={enabled} disabled={!editing} />
				</div>
				<div class="grid gap-2">
					<Label for="notes" class="text-muted-foreground">Notes</Label>
					<Input
						id="notes"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						placeholder="..."
						bind:value={notes}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="wikiLink" class="text-muted-foreground">Wiki Link</Label>
					<Input
						id="wikiLink"
						class="disabled:border-transparent disabled:opacity-100"
						disabled={!editing}
						placeholder="https://example.com/..."
						bind:value={wikiLink}
					/>
				</div>
				<div class="grid gap-2">
					<Label for="requiredQualifications" class="text-muted-foreground"
						>Required Qualifications</Label
					>
					<Select.Root
						type="multiple"
						name="requiredQualifications"
						bind:value={requiredQualifications}
						disabled={!editing}
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

				{#if editing}
					<ErrorText {error} />
					<Button type="submit" class="w-full">Save</Button>
				{/if}
			</div>
		</form>
	</Card.Content>
</Card.Root>
