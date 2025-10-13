<script lang="ts">
	import type { Qualification } from '$lib/api/model/qualification';

	interface Props {
		qualification: Qualification;
	}

	let { qualification }: Props = $props();
</script>

<span
	class="me-1 grow-0 rounded-full border-2 px-2.5 py-0.5 text-xs"
	style="margin-top: 0.125em; margin-bottom: 0.125em; border-color: {qualification.colour};"
>
	{qualification.name}
</span>
