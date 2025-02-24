<script lang="ts">
	let {
		sheetOpen = $bindable(false),
		href,
		children,
		classes = "",
		...others
	} = $props();

	function closeSheet() {
		sheetOpen = false;
	}
</script>

<a class="hover:text-foreground/80 transition-colors text-foreground/60 font-accent {classes}" onclick={closeSheet} {href} {...others}>{@render children()}</a>
