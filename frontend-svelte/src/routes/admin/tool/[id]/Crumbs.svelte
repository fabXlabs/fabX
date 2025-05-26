<script lang="ts">
	// noinspection ES6UnusedImports
	import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
	import { base } from '$app/paths';
	import type { AugmentedUser } from '$lib/api/model/user';

	interface Props {
		user: AugmentedUser;
	}

	let { user }: Props = $props();
</script>

<Breadcrumb.Root>
	<Breadcrumb.List>
		<Breadcrumb.Item>
			<Breadcrumb.Link href="{base}/admin">Admin</Breadcrumb.Link>
		</Breadcrumb.Item>

		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Link href="{base}/admin/user">Users</Breadcrumb.Link>
		</Breadcrumb.Item>

		<Breadcrumb.Separator />

		<Breadcrumb.Item>
			<Breadcrumb.Page>{user.firstName} {user.lastName}</Breadcrumb.Page>
		</Breadcrumb.Item>
	</Breadcrumb.List>
</Breadcrumb.Root>
