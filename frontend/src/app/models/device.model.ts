import { Tool } from "./tool.model";
import { ChangeableValue } from "./changeable-value";

export interface Device {
    id: string,
    aggregateVersion: number,
    name: string,
    background: string,
    backupBackendUrl: string,
    actualFirmwareVersion: string | null,
    desiredFirmwareVersion: string | null,
    attachedTools: Record<number, string>,
}

export interface AugmentedDevice {
    id: string,
    aggregateVersion: number,
    name: string,
    background: string,
    backupBackendUrl: string,
    actualFirmwareVersion: string | null,
    desiredFirmwareVersion: string | null,
    attachedTools: Record<number, Tool>,
}

export interface DeviceCreationDetails {
    name: string,
    background: string,
    backupBackendUrl: string,
    mac: string,
    secret: string
}

export interface DeviceDetails {
    name: ChangeableValue<string> | null
    background: ChangeableValue<string> | null
    backupBackendUrl: ChangeableValue<string> | null
}

export interface DesiredFirmwareVersion {
    desiredFirmwareVersion: string
}

export interface ToolAttachmentDetails {
    toolId: string
}

export interface ToolUnlockDetails {
    toolId: string
}
