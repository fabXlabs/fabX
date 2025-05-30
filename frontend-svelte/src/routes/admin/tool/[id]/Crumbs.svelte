<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
	import { base } from '$app/paths';
	import type { AugmentedTool } from '$lib/api/model/tool';

	interface Props {
		tool: AugmentedTool;
	}

	let { tool }: Props = $props();
</script>

<Breadcrumb.Root>
	<Breadcrumb.List>
		<Breadcrumb.Item>
			<Breadcrumb.Link href="{base}/admin">Admin</Breadcrumb.Link>
		</Breadcrumb.Item>

		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Link href="{base}/admin/tool">Tools</Breadcrumb.Link>
		</Breadcrumb.Item>

		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Page>{tool.name}</Breadcrumb.Page>
		</Breadcrumb.Item>
	</Breadcrumb.List>
</Breadcrumb.Root>
