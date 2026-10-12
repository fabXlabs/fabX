import type { PageLoad } from './$types';
import { getAllQualifications } from '#lib/api/qualifications.js';

export const load: PageLoad = async ({ fetch }) => {
	const qualifications = await getAllQualifications(fetch).catch((error) => {
		console.log('getAllQualifications failed:', error);
		return [];
	});

	return { qualifications };
};
