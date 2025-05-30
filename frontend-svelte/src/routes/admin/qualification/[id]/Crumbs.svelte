<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
	import { base } from '$app/paths';
	import type { Qualification } from '$lib/api/model/qualification';

	interface Props {
		qualification: Qualification;
	}

	let { qualification }: Props = $props();
</script>

<Breadcrumb.Root>
	<Breadcrumb.List>
		<Breadcrumb.Item>
			<Breadcrumb.Link href="{base}/admin">Admin</Breadcrumb.Link>
		</Breadcrumb.Item>

		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Link href="{base}/admin/qualification">Qualifications</Breadcrumb.Link>
		</Breadcrumb.Item>

		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Page>{qualification.name}</Breadcrumb.Page>
		</Breadcrumb.Item>
	</Breadcrumb.List>
</Breadcrumb.Root>
