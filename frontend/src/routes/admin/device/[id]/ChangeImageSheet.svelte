<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Sheet from '$lib/components/ui/sheet/index.js';
	import { changeThumbnail } from '$lib/api/devices';
	import type { FabXError } from '$lib/api/model/error';
	import { invalidateAll } from '$app/navigation';
	import ErrorText from '$lib/components/ErrorText.svelte';

	interface Props {
		id: string;
		open: boolean;
	}

	let { id, open = $bindable() }: Props = $props();

	let files = $state(undefined);

	let error: FabXError | null = $state(null);

	function resetForm() {
		files = undefined;
		error = null;
	}

	async function submit() {
		error = null;

		if (files) {
			for (const file of files as FileList) {
				const res = await changeThumbnail(fetch, id, file).catch((e) => {
					error = e;
					return '';
				});

				if (res) {
					await invalidateAll();
					open = false;
					resetForm();
				}
			}
		}
	}
</script>

<Sheet.Root bind:open>
	<Sheet.Content side="right" class="flex flex-col">
		<Sheet.Header>
			<Sheet.Title>Change Device Thumbnail</Sheet.Title>
		</Sheet.Header>
		<input bind:files type="file" class="file-upload" onchange={submit} />
		<ErrorText {error} />
	</Sheet.Content>
</Sheet.Root>
