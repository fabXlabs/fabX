import type { PageLoad } from './$types';
import { augmentDevice, getDeviceById } from '$lib/api/devices';
import { getAllTools } from '$lib/api/tools';

export const load: PageLoad = async ({ fetch, params }) => {
	const device_ = getDeviceById(fetch, params.id).catch((error) => {
		console.log('getDeviceById failed:', error);
		return null;
	});

	const tools_ = getAllTools(fetch).catch((error) => {
		console.log('getAllTools failed:', error);
		return [];
	});

	const device = await device_;
	if (device) {
		const augmentedDevice = augmentDevice(device, await tools_);
		return { augmentedDevice };
	}

	return {
		augmentedDevice: null
	};
};
