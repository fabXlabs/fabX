import type { PageLoad } from './$types';
import { augmentDevices, getAllDevices } from '$lib/api/devices';
import { getAllTools } from '$lib/api/tools';

export const load: PageLoad = async ({ fetch }) => {
	const devices = await getAllDevices(fetch)
		.catch(error => {
			console.log('getAllDevices failed:', error);
			return [];
		});

	const tools = await getAllTools(fetch)
		.catch(error => {
			console.log('getAllTools failed:', error);
			return [];
		});

	const augmentedDevices = augmentDevices(devices, tools);

	return { augmentedDevices };
};
