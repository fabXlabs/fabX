<script>
	import { resolve } from '$app/paths';
	import { goto } from '$app/navigation';
	import { browser } from '$app/environment';
	import { LoaderCircle } from 'lucide-svelte';

	let { data } = $props();

	// route user to their dashboard depending on their role
	if (data.me) {
		if (data.me.isAdmin) {
			if (browser) {
				goto(resolve('/admin'));
			}
		} else {
			if (browser) {
				goto(resolve('/instructor'));
			}
		}
	}
</script>

<enhanced:img
	src="/static/bg/3.jpeg"
	alt="an art installation"
	class="pointer-events-none absolute h-full w-full object-cover"
/>

<div class="flex grow bg-cover bg-center">
	<div class="flex w-full items-center justify-center">
		<LoaderCircle class="h-16 w-16 animate-spin text-gray-300" />
	</div>
</div>
