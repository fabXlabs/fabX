<script lang="ts">
	import { deviceThumbnailUrl } from '$lib/api/devices';
	import type { ClassValue } from 'clsx';
	import { cn } from '$lib/utils.js';

	interface Props {
		id: string;
		class: ClassValue | null | undefined;
	}

	let { id, class: className }: Props = $props();

	let thumbnailUrl = $derived.by(() => {
		return deviceThumbnailUrl(id);
	});
</script>

<img
	src={thumbnailUrl}
	alt="the device"
	class={cn('aspect-square w-12 rounded-lg border shadow-xs', className)}
/>
