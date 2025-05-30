<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
	import { base } from '$app/paths';
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
	</Breadcrumb.List>
</Breadcrumb.Root>
