import type { PageLoad } from './$types';
import { augmentTools, getAllTools } from '$lib/api/tools';
import type { Tool } from '$lib/api/model/tool';
import { getAllQualifications } from '$lib/api/qualifications';
import type { Qualification } from '$lib/api/model/qualification';

export const load: PageLoad = async ({ fetch }) => {
	let tools_ = await getAllTools(fetch)
		.catch(error => {
			console.log('getAllTools failed:', error);
		});
	let tools: Tool[] = tools_ || [];

	let qualifications_ = await getAllQualifications(fetch)
		.catch(error => {
			console.log('getAllQualifications failed:', error);
		})
	let qualifications: Qualification[] = qualifications_ || [];

	let augmentedTools = augmentTools(tools, qualifications);

	return { augmentedTools };
};
