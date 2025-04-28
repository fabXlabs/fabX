import type { ChangeableValue } from './changeable-value';
import type { Qualification } from '$lib/api/model/qualification';

export interface Tool {
	id: string;
	aggregateVersion: number;
	name: string;
	type: ToolType;
	requires2FA: boolean;
	time: number;
	idleState: IdleState;
	enabled: boolean;
	notes: string | null;
	wikiLink: string;
	requiredQualifications: string[];
}

export interface AugmentedTool {
	id: string;
	aggregateVersion: number;
	name: string;
	type: ToolType;
	requires2FA: boolean;
	time: number;
	idleState: IdleState;
	enabled: boolean;
	notes: string | null;
	wikiLink: string;
	requiredQualifications: Qualification[];
}

export interface ToolCreationDetails {
	name: string;
	type: ToolType;
	requires2FA: boolean;
	time: number;
	idleState: IdleState;
	wikiLink: string;
	requiredQualifications: string[];
}

export interface ToolDetails {
	name: ChangeableValue<string> | null;
	type: ChangeableValue<ToolType> | null;
	requires2FA: ChangeableValue<boolean> | null;
	time: ChangeableValue<number> | null;
	idleState: ChangeableValue<IdleState> | null;
	enabled: ChangeableValue<boolean> | null;
	notes: ChangeableValue<string | null> | null;
	wikiLink: ChangeableValue<string> | null;
	requiredQualifications: ChangeableValue<string[]> | null;
}

export const toolTypes = ['UNLOCK', 'KEEP'];
export type ToolType = (typeof toolTypes)[number];

export const idleStates = ['IDLE_LOW', 'IDLE_HIGH'];
export type IdleState = (typeof idleStates)[number];
