import type { PageLoad } from './$types';
import { getAllUsersLimited } from '#lib/api/users.js';
import { getAllQualifications } from '#lib/api/qualifications.js';

export const load: PageLoad = async ({ fetch }) => {
	const users_ = getAllUsersLimited(fetch).catch(() => {
		return [];
	});
	const qualifications_ = getAllQualifications(fetch).catch(() => {
		return [];
	});
	const users = await users_;
	const qualifications = await qualifications_;
	return { users, qualifications };
};
