<script>
	import { base } from '$app/paths';
	import { goto } from '$app/navigation';
	import { browser } from '$app/environment';
	import bgImg3 from '$lib/assets/bg/3.jpeg';
	import { LoaderCircle } from 'lucide-svelte';

	console.log(`base is ${base}`);

	let { data } = $props();

	// route user to their dashboard depending on their role
	if (data.me) {
		if (data.me.isAdmin) {
			if (browser) {
				goto(`${base}/admin`);
			}
		} else {
			if (browser) {
				goto(`${base}/instructor`);
			}
		}
	}
</script>

<div class="flex grow bg-cover bg-center" style="background-image: url({bgImg3})">
	<div class="flex w-full items-center justify-center">
		<LoaderCircle class="h-16 w-16 animate-spin text-gray-300" />
	</div>
</div>
