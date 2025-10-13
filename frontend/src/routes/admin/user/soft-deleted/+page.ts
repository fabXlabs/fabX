import type { PageLoad } from './$types';
import { getSoftDeletedUsers } from '$lib/api/users';

export const load: PageLoad = async ({ fetch }) => {
	const users = await getSoftDeletedUsers(fetch).catch((error) => {
		console.log('getSoftDeletedUsers failed:', error);
		return [];
	});

	return { users };
};
