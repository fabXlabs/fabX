<script lang="ts">
	import { deviceThumbnailUrl } from '$lib/api/devices';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();

	let thumbnailUrl = $derived.by(() => {
		return deviceThumbnailUrl(id);
	});
</script>

<img src={thumbnailUrl} alt="the device" class="aspect-square w-12 rounded-lg border shadow-xs" />
