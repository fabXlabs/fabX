import type { PageLoad } from './$types';
import { getAllLimitedUsers } from '$lib/api/users';

export const load: PageLoad = async ({ fetch, params }) => {
	const limitedUsersData_ = getAllLimitedUsers(fetch).catch(_ => { return []; });

	return {
		users: await limitedUsersData_
	};
};