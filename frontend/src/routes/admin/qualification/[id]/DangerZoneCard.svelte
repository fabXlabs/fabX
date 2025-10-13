<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Card from '$lib/components/ui/card/index.js';
	import DeleteQualificationAlert from './DeleteQualificationAlert.svelte';
	import type { Qualification } from '$lib/api/model/qualification';

	interface Props {
		qualification: Qualification;
	}

	let { qualification }: Props = $props();
</script>

<Card.Root class="overflow-auto">
	<Card.Header>
		<Card.Title class="text-lg">Danger Zone</Card.Title>
	</Card.Header>
	<Card.Content class="flex flex-col gap-2">
		<DeleteQualificationAlert {qualification} />
	</Card.Content>
</Card.Root>
