export interface Device {
    id: string,
    aggregateVersion: number,
    name: string,
    background: string,
    backupBackendUrl: string,
    attachedTools: Record<number, string>,
}
