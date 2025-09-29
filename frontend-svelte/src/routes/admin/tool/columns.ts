import type { ColumnDef } from '@tanstack/table-core';
import type { AugmentedTool } from '$lib/api/model/tool';
import { renderComponent } from '$lib/components/ui/data-table';
import QualificationTagList from '$lib/components/QualificationTagList.svelte';
import ToolImage from './ToolImage.svelte';

export const columns: ColumnDef<AugmentedTool>[] = [
	{
		accessorKey: 'image',
		header: '',
		cell: ({ row }) => {
			return renderComponent(ToolImage, {
				id: row.original.id,
				class: '',
				onclick: () => {}
			});
		}
	},
	{
		accessorKey: 'name',
		header: 'Name'
	},
	{
		accessorKey: 'type',
		header: 'Type'
	},
	{
		accessorKey: 'requires2FA',
		header: '2FA'
	},
	{
		accessorKey: 'time',
		header: 'Time'
	},
	{
		accessorKey: 'idleState',
		header: 'Idle State'
	},
	{
		accessorKey: 'enabled',
		header: 'Enabled'
	},
	{
		accessorKey: 'notes',
		header: 'Notes'
	},
	{
		accessorKey: 'wikiLink',
		header: 'Wiki'
	},
	{
		accessorKey: 'requiredQualifications',
		header: 'Qualifications',
		cell: ({ row }) => {
			return renderComponent(QualificationTagList, {
				qualifications: row.original.requiredQualifications || []
			});
		}
	}
];
