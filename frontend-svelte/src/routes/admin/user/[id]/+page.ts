import type { PageLoad } from './$types';
import { augmentUser, getUserById } from '$lib/api/users';
import { getAllQualifications } from '$lib/api/qualifications';
import { getAllDevices } from '$lib/api/devices';

export const load: PageLoad = async ({ fetch, params }) => {
	const user_ = getUserById(fetch, params.id).catch((error) => {
		console.log('getUserById failed:', error);
		return null;
	});

	const qualifications_ = getAllQualifications(fetch).catch((error) => {
		console.log('getAllQualifications failed:', error);
		return [];
	});

	const devices_ = getAllDevices(fetch).catch((error) => {
		console.log('getAllDevices failed:', error);
		return [];
	});

	const user = await user_;
	const qualifications = await qualifications_;
	if (user) {
		const augmentedUser = augmentUser(user, qualifications);
		const devices = await devices_;
		return { augmentedUser, devices, qualifications };
	}
	return {
		augmentedUser: null,
		devices: null,
		qualifications: null
	};
};
