<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table/index.js';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import AttachedToolsCardTableActions from './AttachedToolsCardTableActions.svelte';
	import AttachToolSheet from './AttachToolSheet.svelte';
	import type { Tool } from '$lib/api/model/tool';

	interface Props {
		device: AugmentedDevice;
		tools: Tool[];
	}

	let { device, tools }: Props = $props();

	let error: FabXError | null = $state(null);
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Attached Tools</Card.Title>
			<AttachToolSheet {device} {tools} />
		</div>
	</Card.Header>
	<Card.Content>
		<ErrorText {error} />
		<div class="rounded-md border">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Pin</Table.Head>
						<Table.Head>Name</Table.Head>
						<Table.Head></Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each Object.entries(device.attachedTools) as [pin, attachedTool] (pin)}
						<Table.Row>
							<Table.Cell>{pin}</Table.Cell>
							<Table.Cell>{attachedTool.name}</Table.Cell>
							<Table.Cell class="text-right">
								<AttachedToolsCardTableActions
									{device}
									tool={attachedTool}
									pin={Number.parseInt(pin)}
								/>
							</Table.Cell>
						</Table.Row>
					{/each}
					{#if Object.entries(device.attachedTools).length <= 0}
						<Table.Row>
							<Table.Cell colspan={3} class="text-center">No Attached Tools</Table.Cell>
						</Table.Row>
					{/if}
				</Table.Body>
			</Table.Root>
		</div>
	</Card.Content>
</Card.Root>
