import type { PageLoad } from './$types';
import { augmentTools, getAllTools } from '$lib/api/tools';
import { getAllQualifications } from '$lib/api/qualifications';

export const load: PageLoad = async ({ fetch }) => {
	const tools = await getAllTools(fetch)
		.catch(error => {
			console.log('getAllTools failed:', error);
			return [];
		});

	const qualifications = await getAllQualifications(fetch)
		.catch(error => {
			console.log('getAllQualifications failed:', error);
			return [];
		});

	const augmentedTools = augmentTools(tools, qualifications);

	return { augmentedTools };
};
