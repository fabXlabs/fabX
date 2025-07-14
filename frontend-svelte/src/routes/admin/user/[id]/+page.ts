import type { PageLoad } from './$types';
import { augmentUser, getAllUsers, getUserById, getUserSourcingEventsById } from '$lib/api/users';
import { getAllQualifications } from '$lib/api/qualifications';
import { getAllDevices } from '$lib/api/devices';

export const load: PageLoad = async ({ fetch, params }) => {
	const user_ = getUserById(fetch, params.id).catch((error) => {
		console.log('getUserById failed:', error);
		return null;
	});

	const users_ = getAllUsers(fetch);

	const sourcingEvents_ = getUserSourcingEventsById(fetch, params.id).catch((error) => {
		console.log('getUserSourcingEventsById failed:', error);
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
	const users = await users_;
	const sourcingEvents = await sourcingEvents_;
	const qualifications = await qualifications_;
	const devices = await devices_;
	if (user) {
		const augmentedUser = augmentUser(user, qualifications);
		return { augmentedUser, users, sourcingEvents, devices, qualifications };
	}
	return {
		augmentedUser: null,
		users: null,
		devices: null,
		qualifications: null,
		sourcingEvents: null
	};
};
