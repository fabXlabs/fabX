import type { ColumnDef } from '@tanstack/table-core';
import type { AugmentedDevice } from '$lib/api/model/device';
import { renderComponent } from '$lib/components/ui/data-table';
import AttachedToolsList from './AttachedToolsList.svelte';
import DeviceImage from './DeviceImage.svelte';
import DeviceConnectionStatus from './DeviceConnectionStatus.svelte';
import AttachedInputsList from './AttachedInputsList.svelte';

export const columns: ColumnDef<AugmentedDevice>[] = [
	{
		accessorKey: 'image',
		header: '',
		cell: ({ row }) => {
			return renderComponent(DeviceImage, {
				id: row.original.id,
				class: '',
				onclick: () => {}
			});
		}
	},
	{
		accessorKey: 'connectionStatus',
		header: 'Status',
		cell: ({ row }) => {
			return renderComponent(DeviceConnectionStatus, {
				connectionStatus: row.original.connectionStatus
			});
		}
	},
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
		accessorKey: 'attachedInputs',
		header: 'Inputs',
		cell: ({ row }) => {
			return renderComponent(AttachedInputsList, {
				attachedInputs: row.original.attachedInputs,
				pinStatusUpdatedAt: row.original.pinStatus?.updatedAt || null
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
