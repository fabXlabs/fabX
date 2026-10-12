import type { PageLoad } from './$types';
import { getDeviceById } from '#lib/api/devices.js';
import { getToolById } from '#lib/api/tools.js';

export const load: PageLoad = async ({ fetch, params }) => {
	const device_ = getDeviceById(fetch, params.id).catch((error) => {
		console.log('getDeviceById failed:', error);
		return null;
	});

	const tool_ = getToolById(fetch, params.toolid).catch((error) => {
		console.log('getToolById failed:', error);
		return [];
	});

	const device = await device_;
	const tool = await tool_;

	return {
		device,
		tool
	};
};
