<script lang="ts">
	import type { PageProps } from './$types';
	import AdminEntityList from '$lib/components/AdminEntityList.svelte';
	import QualificationList from './QualificationList.svelte';

	let { data }: PageProps = $props();
</script>

<AdminEntityList name="Qualifications">
	<QualificationList qualifications={data.qualifications} />
</AdminEntityList>
