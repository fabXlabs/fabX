<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import type { AugmentedTool } from '$lib/api/model/tool';
	import DeleteToolAlert from './DeleteToolAlert.svelte';

	interface Props {
		tool: AugmentedTool;
	}

	let { tool }: Props = $props();
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<Card.Title class="text-lg">Danger Zone</Card.Title>
	</Card.Header>
	<Card.Content class="flex flex-col gap-2">
		<DeleteToolAlert {tool} />
	</Card.Content>
</Card.Root>
