import type { PageLoad } from './$types';
import { getAllQualifications, type Qualification } from '$lib/qualifications';
import { redirect } from '@sveltejs/kit';
import { UnauthorizedError } from '$lib/deserialize';

export const load: PageLoad = async ({ fetch }) => {
	let qualifications_ = await getAllQualifications(fetch)
		.catch(error => {
			console.log('getAllQualifications failed:', error);
			if (error instanceof UnauthorizedError) {
				redirect(302, '/login');
			}
		});

	let qualifications: Qualification[] = qualifications_ || [];

	return { qualifications };
};
