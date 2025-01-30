<script lang="ts">
	import '../app.css';
	let { children } = $props();

	function onerror(e: any) {
		console.error("global error boundary", e);
	}
</script>

<svelte:boundary {onerror}>
	{@render children()}
</svelte:boundary>
