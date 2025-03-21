import type { PageLoad } from './$types';
import { getAllUsers } from '$lib/api/users';
import { getAllDevices } from '$lib/api/devices';
import { getAllQualifications } from '$lib/api/qualifications';
import { getAllTools } from '$lib/api/tools';

export const load: PageLoad = async ({ fetch, params }) => {
	const devices_ = getAllDevices(fetch).catch(_ => { return []; });
	const qualifications_ = getAllQualifications(fetch).catch(_ => { return []; });
	const tools_ = getAllTools(fetch).catch(_ => { return []; });
	const users_ = getAllUsers(fetch).catch(_ => { return []; });

	const devices = await devices_;
	const qualifications = await qualifications_;
	const tools = await tools_;
	const users = await users_;

	return {
		deviceCount: devices.length,
		qualificationCount: qualifications.length,
		toolCount: tools.length,
		userCount: users.length
	};
};