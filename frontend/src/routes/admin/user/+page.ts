import type { PageLoad } from './$types';
import { getAllUsers } from '$lib/api/users';
import type { User } from '$lib/api/model/user';

export const load: PageLoad = async ({ fetch }) => {
	let users_ = await getAllUsers(fetch)
		.catch(error => {
			console.log('getAllUsers failed:', error);
		});

	let users: User[] = users_ || [];
	return { users };
};
