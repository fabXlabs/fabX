<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	// noinspection ES6UnusedImports
	import * as Select from '$lib/components/ui/select/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import type { FabXError } from '$lib/api/model/error';
	import ErrorText from '$lib/components/ErrorText.svelte';
	import { buttonVariants } from '$lib/components/ui/button/index.js';
	import { attachTool } from '$lib/api/devices';
	import type { AugmentedDevice } from '$lib/api/model/device';
	import type { Tool } from '$lib/api/model/tool';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		device: AugmentedDevice;
		tools: Tool[];
	}

	let { device, tools }: Props = $props();

	let sheetOpen = $state(false);

	let pin = $state(0);
	let toolId: string = $state('');

	let error: FabXError | null = $state(null);

	const sortedTools = $derived.by(() => {
		return tools.toSorted((a, b) => (a.name < b.name ? -1 : 1));
	});

	const toolTriggerContent = $derived.by(() => {
		return tools.find((t) => t.id == toolId)?.name || 'Choose Tool';
	});

	async function submit() {
		error = null;

		const res = await attachTool(fetch, device.id, pin, toolId).catch((e) => {
			error = e;
			return '';
		});

		if (res) {
			await invalidateAll();
			pin = 0;
			toolId = '';
			sheetOpen = false;
		}
	}
</script>

{@debug tools}
{@debug sortedTools}

<Sheet.Root bind:open={sheetOpen}>
	<Sheet.Trigger class={buttonVariants({ variant: 'outline' })}>Attach Tool</Sheet.Trigger>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Attach Tool</Sheet.Title>
		</Sheet.Header>
		<form onsubmit={submit}>
			<div class="grid gap-4 py-4">
				<div class="grid gap-2">
					<Label for="pin">Pin</Label>
					<Input id="pin" type="number" min="0" max="15" bind:value={pin} />
				</div>
				<div class="grid gap-2">
					<Label for="tool" class="text-muted-foreground">Tool</Label>
					<Select.Root type="single" name="tool" bind:value={toolId}>
						<Select.Trigger
							class="w-full disabled:border-transparent disabled:opacity-100 disabled:shadow-none"
						>
							{toolTriggerContent}
						</Select.Trigger>
						<Select.Content>
							{#each sortedTools as tool (tool.id)}
								<Select.Item value={tool.id}>
									{tool.name}
								</Select.Item>
							{/each}
						</Select.Content>
					</Select.Root>
				</div>
			</div>

			<ErrorText {error} />

			<Sheet.Footer>
				<Button type="submit" class="w-full">Attach</Button>
			</Sheet.Footer>
		</form>
	</Sheet.Content>
</Sheet.Root>
