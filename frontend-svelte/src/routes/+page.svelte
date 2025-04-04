<script>
	import { base } from '$app/paths';
	import { Button } from '$lib/components/ui/button/';
    import { onMount } from 'svelte';
	import { goto } from '$app/navigation';

  console.log(`base is ${base}`)

  let { data } = $props();

  onMount(() => {
		// Handle redirection based on user type
		if (data.me) {
			if (data.me.isAdmin) {
				goto(`${base}/admin`);
			} else {
				goto(`${base}/instructor`);
			}
		} else {
			// No user data means not logged in
			goto(`${base}/login`);
		}
	});
</script>

<svelte:head>
	<title>fabX | Loading</title>
</svelte:head>

 <!-- TOOD: Add a skeleton here -->
<!-- Skeleton loader while redirecting -->
<!-- <div class="container flex items-center justify-center min-h-screen">
	<div class="w-full max-w-md space-y-8">
		<div class="flex flex-col items-center">
			<Skeleton class="h-12 w-48 mb-4" />
			<Skeleton class="h-6 w-72" />
		</div>

		<div class="space-y-4 mt-8">
			<Skeleton class="h-16 w-full" />
			<Skeleton class="h-16 w-full" />
			<Skeleton class="h-16 w-full" />
		</div>
	</div>
</div> -->
