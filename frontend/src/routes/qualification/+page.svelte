<script lang="ts">
	import type { PageProps } from './$types';
	import QualificationList from './QualificationList.svelte';

	let { data }: PageProps = $props();
</script>

<div class="prose">
	<h1>Qualifications</h1>
	<QualificationList qualifications={data.qualifications} />
</div>
