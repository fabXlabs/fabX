<script lang="ts">
	interface Props {
		name: String,
		children: any
	}
	let { name, children }: Props = $props();
</script>

<div class="container relative max-w-screen-2xl mt-7">
	<h2 class="font-accent text-2xl my-2">{name}</h2>
	{@render children()}
</div>
