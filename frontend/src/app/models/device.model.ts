import { Tool } from "./tool.model";
import { ChangeableValue } from "./changeable-value";

export interface Device {
    id: string,
    aggregateVersion: number,
    name: string,
    background: string,
    backupBackendUrl: string,
    attachedTools: Record<number, string>,
}

export interface AugmentedDevice {
    id: string,
    aggregateVersion: number,
    name: string,
    background: string,
    backupBackendUrl: string,
    attachedTools: Record<number, Tool>,
}

export interface DeviceDetails {
    name: ChangeableValue<string> | null
    background: ChangeableValue<string> | null
    backupBackendUrl: ChangeableValue<string> | null
}

export interface ToolAttachmentDetails {
    toolId: string
}
