import type { PageLoad } from './$types';
import { augmentDevices, getAllDevices } from '$lib/api/devices';
import type { Device } from '$lib/api/model/device';
import { getAllTools } from '$lib/api/tools';
import type { Tool } from '$lib/api/model/tool';

export const load: PageLoad = async ({ fetch }) => {
	let devices_ = await getAllDevices(fetch)
		.catch(error => {
			console.log('getAllDevices failed:', error);
		});
	let devices: Device[] = devices_ || [];

	let tools_ = await getAllTools(fetch)
		.catch(error => {
			console.log('getAllTools failed:', error);
		});
	let tools: Tool[] = tools_ || [];

	let augmentedDevices = augmentDevices(devices, tools);

	return { augmentedDevices };
};
