import type { PageLoad } from '../../../../.svelte-kit/types/src/routes/admin/qualification/$types';
import { getAllQualifications } from '$lib/api/qualifications';
import type { Qualification } from '$lib/api/model/qualification';

export const load: PageLoad = async ({ fetch }) => {
	let qualifications_ = await getAllQualifications(fetch)
		.catch(error => {
			console.log('getAllQualifications failed:', error);
		});

	let qualifications: Qualification[] = qualifications_ || [];

	return { qualifications };
};
