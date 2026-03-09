<script lang="ts">
	import { Action } from '$lib/components/ui/alert-dialog/index.js';
	import type { AlertActionProps } from '$lib/components/ui/alert-dialog/index.js';
	import { Spinner } from '$lib/components/ui/spinner';
	import { cn } from '$lib/utils';
	import { buttonVariants } from '$lib/components/ui/button';

	let {
		ref = $bindable(null),
		class: className,
		working = $bindable(false),
		children,
		...restProps
	}: AlertActionProps = $props();
</script>

<Action
	bind:ref
	class={cn(buttonVariants(), 'sm:w-1/2', className)}
	disabled={working}
	{...restProps}
>
	{#if working}
		<Spinner />
	{/if}
	{@render children?.()}
</Action>
