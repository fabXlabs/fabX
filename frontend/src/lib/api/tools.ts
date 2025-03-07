import { baseUrl, type FetchFunction } from '$lib/api';
import type { AugmentedTool, Tool } from '$lib/api/model/tool';
import { deserialize } from '$lib/api/deserialize';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';

export async function getAllTools(fetch: FetchFunction): Promise<Tool[]> {
	console.debug('getAllTools...');

	return await fetch(`${baseUrl}/tool`, { credentials: 'include' })
		.then(deserialize<Tool[]>);
}

export function augmentTools(tools: Tool[], qualifications: Qualification[]): AugmentedTool[] {
	const getQualifications = augmentQualifications(qualifications);

	return tools.map(t => ({
		...t,
		requiredQualifications: getQualifications(t.requiredQualifications)
	}));
}
