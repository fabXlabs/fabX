import type { PageLoad } from './$types';
import { augmentTool, getToolById } from '$lib/api/tools';
import { getAllQualifications } from '$lib/api/qualifications';

export const load: PageLoad = async ({ fetch, params }) => {
	const tool_ = getToolById(fetch, params.id).catch((error) => {
		console.log('getUserById failed:', error);
		return null;
	});

	const qualifications_ = getAllQualifications(fetch).catch((error) => {
		console.log('getAllQualifications failed:', error);
		return [];
	});

	const tool = await tool_;
	if (tool) {
		const augmentedTool = augmentTool(tool, await qualifications_);
		return { augmentedTool };
	}
	return {
		augmentedTool: null
	};
};
