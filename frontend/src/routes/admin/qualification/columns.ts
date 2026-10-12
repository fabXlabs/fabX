import type { ColumnDef } from '@tanstack/table-core';
import { renderComponent } from '#lib/components/ui/data-table/index.js';
import type { Qualification } from '#lib/api/model/qualification.js';
import QualificationTag from '#lib/components/QualificationTag.svelte';

export const columns: ColumnDef<Qualification>[] = [
	{
		accessorKey: 'orderNr',
		header: 'Order Nr.'
	},
	{
		accessorKey: 'name',
		header: 'Name',
		cell: ({ row }) => {
			return renderComponent(QualificationTag, { qualification: row.original });
		}
	},
	{
		accessorKey: 'description',
		header: 'Description'
	}
];
