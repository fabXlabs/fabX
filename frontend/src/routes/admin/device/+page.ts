import type { PageLoad } from './$types';
import {
	augmentDevices,
	getAllDeviceConnectionStatuses,
	getAllDevicePinStatuses,
	getAllDevices
} from '$lib/api/devices';
import { getAllTools } from '$lib/api/tools';

export const load: PageLoad = async ({ fetch }) => {
	const devices = getAllDevices(fetch).catch((error) => {
		console.log('getAllDevices failed:', error);
		return [];
	});

	const tools = getAllTools(fetch).catch((error) => {
		console.log('getAllTools failed:', error);
		return [];
	});

	const deviceConnectionStatuses = getAllDeviceConnectionStatuses(fetch);

	const devicePinStatuses = getAllDevicePinStatuses(fetch);

	const augmentedDevices = augmentDevices(
		await devices,
		await tools,
		await deviceConnectionStatuses,
		await devicePinStatuses
	);

	return { augmentedDevices };
};
