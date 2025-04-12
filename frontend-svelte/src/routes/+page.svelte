<script>
	import { base } from '$app/paths';
	import { goto } from '$app/navigation';
	import bgImg3 from '$lib/assets/bg/3.jpeg';
	import { LoaderCircle } from 'lucide-svelte';
	import { onMount } from 'svelte';

  	console.log(`base is ${base}`)

  	let { data } = $props();

	// route user to their dasboard depending on their role
	if (data.me) {
			if (data.me.isAdmin) {
				goto(`${base}/admin`);
			} else {
				goto(`${base}/instructor`);
			}
		}


</script>

<div class="flex grow bg-cover bg-center" style="background-image: url({bgImg3})">
	  	<div class="flex w-full justify-center items-center">
			<LoaderCircle class="text-gray-300 w-16 h-16 animate-spin" />
		</div>
</div>
