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

	const deviceConnectionStatuses = getAllDeviceConnectionStatuses(fetch).catch((error) => {
		console.log('getAllDeviceConnectionStatuses failed:', error);
		return new Map();
	});

	const devicePinStatuses = getAllDevicePinStatuses(fetch).catch((error) => {
		console.log('getAllDevicePinStatuses failed:', error);
		return new Map();
	});

	const augmentedDevices = augmentDevices(
		await devices,
		await tools,
		await deviceConnectionStatuses,
		await devicePinStatuses
	);

	return { augmentedDevices };
};
