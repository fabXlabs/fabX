import type { PageLoad } from './$types';
import { getQualificationById } from '$lib/api/qualifications';

export const load: PageLoad = async ({ fetch, params }) => {
	const qualification = await getQualificationById(fetch, params.id)
		.catch(error => {
			console.log('getQualificationById failed:', error);
			return null;
		});
	return { qualification };
};
