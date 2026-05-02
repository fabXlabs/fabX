<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table/index.js';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import AttachInputSheet from './AttachInputSheet.svelte';
	import AttachedInputsCardTableActions from './AttachedInputsCardTableActions.svelte';
	import InputStatusRow from '../InputStatusRow.svelte';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();

	let error: FabXError | null = $state(null);
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<div class="flex items-center justify-between">
			<Card.Title class="text-lg">Attached Inputs</Card.Title>
			<AttachInputSheet {device} />
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
						<Table.Head>Status</Table.Head>
						<Table.Head></Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each Object.entries(device.attachedInputs) as [pin, inputDescription] (pin)}
						<Table.Row>
							<Table.Cell>{pin}</Table.Cell>
							<Table.Cell>{inputDescription.name}</Table.Cell>
							<Table.Cell>
								<table>
									<tbody>
										<InputStatusRow {pin} {inputDescription} />
									</tbody>
								</table>
							</Table.Cell>
							<Table.Cell class="text-right">
								<AttachedInputsCardTableActions {device} pin={Number.parseInt(pin)} />
							</Table.Cell>
						</Table.Row>
					{/each}
					{#if Object.entries(device.attachedInputs).length <= 0}
						<Table.Row>
							<Table.Cell colspan={4} class="text-center">No Attached Inputs</Table.Cell>
						</Table.Row>
					{/if}
				</Table.Body>
			</Table.Root>
		</div>
	</Card.Content>
</Card.Root>
