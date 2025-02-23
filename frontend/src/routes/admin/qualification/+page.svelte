<script lang="ts">
	import type { PageProps } from '../../../../.svelte-kit/types/src/routes/admin/qualification/$types';
	import QualificationList from './QualificationList.svelte';

	let { data }: PageProps = $props();
</script>

<div class="container relative prose max-w-screen-2xl mt-7">
	<h2 class="font-accent">Qualifications</h2>
	<QualificationList qualifications={data.qualifications} />
</div>
