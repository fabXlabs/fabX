<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Breadcrumb from '#lib/components/ui/breadcrumb/index.js';
	import { resolve } from '$app/paths';
	import type { AugmentedDevice } from '#lib/api/model/device.js';

	interface Props {
		device: AugmentedDevice;
	}

	let { device }: Props = $props();
</script>

<Breadcrumb.Root>
	<Breadcrumb.List>
		<Breadcrumb.Item
			><Breadcrumb.Link href={resolve('/admin')}>Admin</Breadcrumb.Link></Breadcrumb.Item
		>
		<Breadcrumb.Separator />
		<Breadcrumb.Item
			><Breadcrumb.Link href={resolve('/admin/device')}>Devices</Breadcrumb.Link></Breadcrumb.Item
		>
		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Page>{device.name}</Breadcrumb.Page>
		</Breadcrumb.Item>
	</Breadcrumb.List>
</Breadcrumb.Root>
