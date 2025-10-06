import type { ColumnDef } from '@tanstack/table-core';
import type { AugmentedUser } from '$lib/api/model/user';
import { renderComponent } from '$lib/components/ui/data-table';
import AdminShield from './AdminShield.svelte';
import QualificationTagList from '$lib/components/QualificationTagList.svelte';

export const columns: ColumnDef<AugmentedUser>[] = [
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
		header: 'Wiki Name'
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
		header: 'Qualifications',
		cell: ({ row }) => {
			return renderComponent(QualificationTagList, {
				qualifications: row.original.memberQualifications
			});
		},
		filterFn: (row, columnId, filterValue) => {
			const fV = filterValue as Array<string>;
			return fV.every((v) => !!row.original.memberQualifications.find((q) => q.id === v));
		}
	},
	{
		accessorKey: 'instructorQualifications',
		header: 'Instructor',
		cell: ({ row }) => {
			return renderComponent(QualificationTagList, {
				qualifications: row.original.instructorQualifications || []
			});
		},
		filterFn: (row, columnId, filterValue) => {
			const fV = filterValue as Array<string>;
			return fV.every(
				(v) => !!row.original.instructorQualifications?.find((q) => q.id === v)
			);
		}
	}
];
