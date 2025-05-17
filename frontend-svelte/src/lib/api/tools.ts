import { type FetchFunction } from '$lib/api';
import type { AugmentedTool, Tool } from '$lib/api/model/tool';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';
import { getRequest } from '$lib/api/common';

export async function getAllTools(fetch: FetchFunction): Promise<Tool[]> {
	console.debug('getAllTools...');
	return await getRequest(fetch, '/tool');
}

export async function getToolById(fetch: FetchFunction, id: string): Promise<Tool> {
	console.debug(`getToolById(${id})`);
	return await getRequest(fetch, `/tool/${id}`);
}

export function augmentTool(tool: Tool, qualifications: Qualification[]): AugmentedTool {
	const getQualifications = augmentQualifications(qualifications);

	return {
		...tool,
		requiredQualifications: getQualifications(tool.requiredQualifications)
	};
}

export function augmentTools(tools: Tool[], qualifications: Qualification[]): AugmentedTool[] {
	const getQualifications = augmentQualifications(qualifications);

	return tools.map((t) => ({
		...t,
		requiredQualifications: getQualifications(t.requiredQualifications)
	}));
}
