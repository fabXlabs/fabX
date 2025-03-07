import type { PageLoad } from './$types';
import { augmentUsers, getAllUsers } from '$lib/api/users';
import type { User } from '$lib/api/model/user';
import { getAllQualifications } from '$lib/api/qualifications';
import type { Qualification } from '$lib/api/model/qualification';

export const load: PageLoad = async ({ fetch }) => {
	let users_ = await getAllUsers(fetch)
		.catch(error => {
			console.log('getAllUsers failed:', error);
		});
	let users: User[] = users_ || [];

	let qualifications_ = await getAllQualifications(fetch)
		.catch(error => {
			console.log('getAllQualifications failed:', error);
		})
	let qualifications: Qualification[] = qualifications_ || [];

	let augmentedUsers = augmentUsers(users, qualifications);

	return { augmentedUsers };
};
