import type { PageLoad } from './$types';
import { getAllTools } from '$lib/api/tools';
import type { Tool } from '$lib/api/model/tool';

export const load: PageLoad = async ({ fetch }) => {
	let tools_ = await getAllTools(fetch)
		.catch(error => {
			console.log('getAllTools failed:', error);
		});

	let tools: Tool[] = tools_ || [];
	return { tools };
};
