import { Qualification } from "./qualification.model";
import { ChangeableValue } from "./changeable-value";

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

export interface ToolDetails {
    name: ChangeableValue<string> | null,
    type: ChangeableValue<ToolType> | null,
    time: ChangeableValue<number> | null,
    idleState: ChangeableValue<IdleState> | null,
    enabled: ChangeableValue<boolean> | null,
    wikiLink: ChangeableValue<string> | null,
    requiredQualifications: ChangeableValue<string[]> | null,
}

export const toolTypes = ["UNLOCK", "KEEP"]
export type ToolType = typeof toolTypes[number];

export const idleStates = ["IDLE_LOW", "IDLE_HIGH"];
export type IdleState = typeof idleStates[number];
