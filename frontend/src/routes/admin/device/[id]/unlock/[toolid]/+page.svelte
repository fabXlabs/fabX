<script lang="ts">
	import type { FabXError } from '$lib/api/model/error';
	import { unlockTool } from '$lib/api/devices';
	import { CircleCheckBigIcon, LoaderCircle } from 'lucide-svelte';
	import type { PageProps } from './$types';
	import ErrorText from '$lib/components/ErrorText.svelte';

	let { data }: PageProps = $props();

	let error: FabXError | null = $state(null);

	let unlockSuccess = $state(false);

	$effect(() => {
		unlockTool_();
	});

	async function unlockTool_(): Promise<string> {
		error = null;
		return await unlockTool(fetch, data.device.id, data.tool.id)
			.then((s) => {
				unlockSuccess = true;
				return s;
			})
			.catch((e) => {
				error = e;
				return '';
			});
	}
</script>

<div class="relative container mt-5 max-w-(--breakpoint-2xl)">
	{#if data.device && data.tool}
		<div class="flex justify-between">
			<div>
				<h1 class="font-accent mt-4 mb-2 text-3xl">
					Unlocking {data.tool.name}...
				</h1>
			</div>
		</div>

		<div class="my-6 grid gap-4">
			<div class="my-12 flex grow">
				<div class="flex w-full items-center justify-center">
					{#if unlockSuccess}
						<CircleCheckBigIcon color="green" size="64" />
					{:else if !error}
						<LoaderCircle class="h-16 w-16 animate-spin text-gray-300" />
					{/if}

					<ErrorText {error} />
				</div>
			</div>
		</div>
	{/if}
</div>
