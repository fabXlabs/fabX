import type { PageLoad } from './$types';
import { augmentUser, getUserById } from '$lib/api/users';
import { getAllQualifications } from '$lib/api/qualifications';

export const load: PageLoad = async ({ fetch, params }) => {
	const user_ = getUserById(fetch, params.id)
		.catch(error => {
			console.log('getUserById failed:', error);
			return null;
		});

	const qualifications_ = getAllQualifications(fetch)
		.catch(error => {
			console.log('getAllQualifications failed:', error);
			return [];
		});

	const user = await user_;
	if (user) {
		const augmentedUser = augmentUser(user, await qualifications_);
		return { augmentedUser };
	}
	return {
		augmentedUser: null
	};
};
