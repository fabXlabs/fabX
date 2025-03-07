import { baseUrl, type FetchFunction } from '$lib/api';
import type { AugmentedUser, User } from '$lib/api/model/user';
import { deserialize } from '$lib/api/deserialize';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';

export async function getMe(fetch: FetchFunction): Promise<User> {
	console.debug('getMe...');

	return await fetch(`${baseUrl}/user/me`, { credentials: 'include' })
		.then(deserialize<User>);
}

export async function getAllUsers(fetch: FetchFunction): Promise<User[]> {
	console.debug('getAllUsers...');

	return await fetch(`${baseUrl}/user`, { credentials: 'include' })
		.then(deserialize<User[]>);
}

export function augmentUsers(users: User[], qualifications: Qualification[]): AugmentedUser[] {
	const getQualifications = augmentQualifications(qualifications);

	return users.map(u => ({
		...u,
		memberQualifications: getQualifications(u.memberQualifications),
		instructorQualifications: getQualifications(u.instructorQualifications || [])
	}));
}
