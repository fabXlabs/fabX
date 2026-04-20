import type { ChangeableValue } from './changeable-value';
import type { Tool } from '$lib/api/model/tool';

export interface Device {
	id: string;
	aggregateVersion: number;
	name: string;
	background: string;
	backupBackendUrl: string;
	actualFirmwareVersion: string | null;
	desiredFirmwareVersion: string | null;
	attachedTools: Record<number, string>;
}

export interface AugmentedDevice {
	id: string;
	aggregateVersion: number;
	name: string;
	background: string;
	backupBackendUrl: string;
	actualFirmwareVersion: string | null;
	desiredFirmwareVersion: string | null;
	attachedTools: Record<number, Tool>;
	connectionStatus: boolean | null;
	pinStatus: PinStatus | null;
}

export interface PinStatus {
	inputPinStatus: Map<string, InputPinStatus>;
	updatedAt: string;
}

export const inputPinStatus = ['INPUT_LOW', 'INPUT_HIGH'];
export type InputPinStatus = (typeof inputPinStatus)[number];

export interface DeviceCreationDetails {
	name: string;
	background: string;
	backupBackendUrl: string;
	mac: string;
	secret: string;
}

export interface DeviceDetails {
	name: ChangeableValue<string> | null;
	background: ChangeableValue<string> | null;
	backupBackendUrl: ChangeableValue<string> | null;
}

export interface DesiredFirmwareVersion {
	desiredFirmwareVersion: string;
}

export interface ToolAttachmentDetails {
	toolId: string;
}

export interface ToolUnlockDetails {
	toolId: string;
}

export interface AtDeviceCardCreationDetails {
	userId: string;
}
