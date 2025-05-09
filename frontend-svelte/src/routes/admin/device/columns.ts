import type { ColumnDef } from '@tanstack/table-core';
import type { AugmentedDevice } from '$lib/api/model/device';
import { renderComponent } from '$lib/components/ui/data-table';
import AttachedToolsList from './AttachedToolsList.svelte';

export const columns: ColumnDef<AugmentedDevice>[] = [
	{
		accessorKey: 'name',
		header: 'Name'
	},
	{
		accessorKey: 'background',
		header: 'Background'
	},
	{
		accessorKey: 'backupBackendUrl',
		header: 'Backup Backend URL'
	},
	{
		accessorKey: 'attachedTools',
		header: 'Tools',
		cell: ({ row }) => {
			return renderComponent(AttachedToolsList, {
				attachedTools: row.original.attachedTools
			});
		}
	},
	{
		accessorKey: 'actualFirmwareVersion',
		header: 'Firmware'
	},
	{
		accessorKey: 'desiredFirmwareVersion',
		header: 'Desired Firmware Version'
	}
];
