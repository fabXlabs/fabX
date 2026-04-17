import type { PageLoad } from './$types';
import {
	augmentDevice,
	getDeviceById,
	getDeviceConnectionStatusById,
	getDevicePinStatusById
} from '$lib/api/devices';
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

	const connectionStatus_ = getDeviceConnectionStatusById(fetch, params.id);

	const pinStatus_ = getDevicePinStatusById(fetch, params.id);

	const device = await device_;
	const tools = await tools_;
	const connectionStatus = await connectionStatus_;
	const pinStatus = await pinStatus_;

	if (device) {
		const augmentedDevice = augmentDevice(device, tools, connectionStatus, pinStatus);
		return { augmentedDevice, tools };
	}

	return {
		augmentedDevice: null
	};
};
