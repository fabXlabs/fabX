import type { PageLoad } from './$types';
import { getAllUsers } from '#lib/api/users.js';
import { getAllDevices } from '#lib/api/devices.js';
import { getAllQualifications } from '#lib/api/qualifications.js';
import { getAllTools } from '#lib/api/tools.js';

export const load: PageLoad = async ({ fetch }) => {
	const devices_ = getAllDevices(fetch).catch(() => {
		return [];
	});
	const qualifications_ = getAllQualifications(fetch).catch(() => {
		return [];
	});
	const tools_ = getAllTools(fetch).catch(() => {
		return [];
	});
	const users_ = getAllUsers(fetch).catch(() => {
		return [];
	});

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
