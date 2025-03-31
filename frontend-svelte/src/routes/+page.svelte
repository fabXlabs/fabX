<script>
	import { base } from '$app/paths';
	import { Button } from '$lib/components/ui/button/';

  console.log(`base is ${base}`)
</script>

<div class="container">
	<h1 class="font-accent italic text-5xl my-4">fabX</h1>
	<h2 class="font-accent text-2xl my-2">Access System for Makerspaces</h2>

	<div class="flex flex-col my-4">
		<Button class="my-4 p-8" href="{base}/admin">Admin</Button>
		<Button class="my-4 p-8 bg-gray-800 hover:bg-gray-700" href="https://github.com/fabXlabs/fabX" target="_blank" rel="noopener noreferrer">GitHub</Button>
	</div>
</div>
