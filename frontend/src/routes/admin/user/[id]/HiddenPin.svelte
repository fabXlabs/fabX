<script lang="ts">
	import { Eye } from 'lucide-svelte';
	import { EyeOff } from 'lucide-svelte';
	interface Props {
		pin: string;
	}

	let { pin }: Props = $props();

	let hidden = $state(true);
</script>

{#if hidden}
	<span class="font-mono"
		>**** <Eye class="inline" size={20} onclick={() => (hidden = false)} /></span
	>
{:else}
	<span class="font-mono"
		>{pin} <EyeOff class="inline" size={20} onclick={() => (hidden = true)} /></span
	>
{/if}
