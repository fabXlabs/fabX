<script lang="ts">
	import { toolThumbnailUrl } from '$lib/api/tools';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();

	let thumbnailUrl = $derived.by(() => {
		return toolThumbnailUrl(id);
	});
</script>

<img src={thumbnailUrl} alt="the tool" class="aspect-square w-12 rounded-lg border shadow-xs" />
