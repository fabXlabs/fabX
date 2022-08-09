import { Qualification } from "./qualification.model";

export interface Tool {
    id: string,
    aggregateVersion: number,
    name: string,
    type: ToolType,
    time: number,
    idleState: IdleState,
    enabled: Boolean,
    wikiLink: string,
    requiredQualifications: string[]
}

export interface AugmentedTool {
    id: string,
    aggregateVersion: number,
    name: string,
    type: ToolType,
    time: number,
    idleState: IdleState,
    enabled: Boolean,
    wikiLink: string,
    requiredQualifications: Qualification[]
}

export type ToolType = "UNLOCK" | "KEEP";
export type IdleState = "IDLE_LOW" | "IDLE_HIGH";
