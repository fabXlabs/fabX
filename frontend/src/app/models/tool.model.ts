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

export interface ToolCreationDetails {
    name: string,
    type: ToolType,
    time: number,
    idleState: IdleState,
    wikiLink: string,
    requiredQualifications: string[]
}

export const toolTypes = ["UNLOCK", "KEEP"]
export type ToolType = typeof toolTypes[number];

export const idleStates = ["IDLE_LOW", "IDLE_HIGH"];
export type IdleState = typeof idleStates[number];
