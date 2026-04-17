<script lang="ts">
	import { Wifi, WifiOff, WifiSync } from 'lucide-svelte';

	interface Props {
		connectionStatus: boolean | null;
		size?: number;
	}

	let { connectionStatus, size = 16 }: Props = $props();
</script>

{#if connectionStatus === true}
	<Wifi class="deviceConnected" {size} />
{:else if connectionStatus === false}
	<WifiOff class="deviceDisconnected" {size} />
{:else}
	<WifiSync class="deviceConnectionUnknown" {size} />
{/if}

<style>
	:global .deviceDisconnected {
		color: var(--color-red-700);
	}

	:global .deviceConnected {
		color: var(--color-green-700);
	}

	:global .deviceConnectionUnknown {
		color: var(--color-gray-700);
	}
</style>
