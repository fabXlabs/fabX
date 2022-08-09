import { Tool } from "./tool.model";

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
