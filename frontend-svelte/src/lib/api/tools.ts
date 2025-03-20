import { baseUrl, type FetchFunction } from '$lib/api';
import type { AugmentedTool, Tool } from '$lib/api/model/tool';
import { mapError } from '$lib/api/map-error';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';

export async function getAllTools(fetch: FetchFunction): Promise<Tool[]> {
	console.debug('getAllTools...');
	const res = await fetch(`${baseUrl}/tool`, { credentials: 'include' })
		.then(mapError);
	return res.json();
}

export function augmentTools(tools: Tool[], qualifications: Qualification[]): AugmentedTool[] {
	const getQualifications = augmentQualifications(qualifications);

	return tools.map(t => ({
		...t,
		requiredQualifications: getQualifications(t.requiredQualifications)
	}));
}
