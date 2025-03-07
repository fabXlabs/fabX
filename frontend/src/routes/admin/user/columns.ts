import type { ColumnDef } from '@tanstack/table-core';
import type { User } from '$lib/api/model/user';
import { renderComponent } from '$lib/components/ui/data-table';
import AdminShield from './AdminShield.svelte';

export const columns: ColumnDef<User>[] = [
	{
		accessorKey: 'isAdmin',
		header: '',
		cell: ({ row }) => {
			return renderComponent(AdminShield, { isAdmin: row.original.isAdmin });
		},
		enableHiding: false
	},
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
		header: 'Wiki Name',
	},
	{
		accessorKey: 'locked',
		header: 'Locked'
	},
	{
		accessorKey: 'notes',
		header: 'Notes'
	},
	{
		accessorKey: 'memberQualifications',
		header: 'Qualifications'
	},
	{
		accessorKey: 'instructorQualifications',
		header: 'Instructor'
	}
];
