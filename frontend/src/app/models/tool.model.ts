export interface Tool {
    id: string,
    aggregateVersion: number,
    type: ToolType,
    time: number,
    idleState: IdleState,
    enabled: Boolean,
    wikiLink: string,
    requiredQualifications: string[]
}

export type ToolType = "UNLOCK" | "KEEP";
export type IdleState = "IDLE_LOW" | "IDLE_HIGH";
