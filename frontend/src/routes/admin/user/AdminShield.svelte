<script lang="ts">
	import { Shield } from 'lucide-svelte';

	interface Props {
		isAdmin: boolean;
	}

	let { isAdmin }: Props = $props();
</script>

{#if isAdmin}
	<Shield />
{/if}
