import type { PageLoad } from './$types';
import { augmentUsers, getAllUsers } from '#lib/api/users.js';
import { getAllQualifications } from '#lib/api/qualifications.js';

export const load: PageLoad = async ({ fetch }) => {
	const users = await getAllUsers(fetch).catch((error) => {
		console.log('getAllUsers failed:', error);
		return [];
	});

	const qualifications = await getAllQualifications(fetch).catch((error) => {
		console.log('getAllQualifications failed:', error);
		return [];
	});

	const augmentedUsers = augmentUsers(users, qualifications);

	return { augmentedUsers, qualifications };
};
