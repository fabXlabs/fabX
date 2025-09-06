import { type FetchFunction } from '$lib/api';
import type { AugmentedTool, Tool, ToolCreationDetails, ToolDetails } from '$lib/api/model/tool';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';
import { deleteRequest, getRequest, postRequest, putRequest } from '$lib/api/common';

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

export async function addTool(fetch: FetchFunction, details: ToolCreationDetails): Promise<string> {
	return await postRequest(fetch, '/tool', 'unknown', details);
}

export async function changeToolDetails(
	fetch: FetchFunction,
	id: string,
	details: ToolDetails
): Promise<string> {
	return await putRequest(fetch, `/tool/${id}`, id, details);
}

export async function deleteTool(fetch: FetchFunction, id: string): Promise<string> {
	return await deleteRequest(fetch, `/tool/${id}`, id);
}
