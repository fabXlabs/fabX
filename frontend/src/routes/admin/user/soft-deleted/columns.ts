import type { ColumnDef } from '@tanstack/table-core';
import type { User } from '$lib/api/model/user';
import { renderComponent } from '$lib/components/ui/data-table';
import DataTableActions from './DataTableActions.svelte';

export const columns: ColumnDef<User>[] = [
	{
		accessorKey: 'firstName',
		header: 'First Name',
		enableSorting: true
	},
	{
		accessorKey: 'lastName',
		header: 'Last Name',
		enableSorting: true
	},
	{
		accessorKey: 'wikiName',
		header: 'Wiki Name'
	},
	{
		id: 'actions',
		cell: ({ row }) => {
			return renderComponent(DataTableActions, { user: row.original });
		}
	}
];
