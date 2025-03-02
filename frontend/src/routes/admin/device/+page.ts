import type { PageLoad } from './$types';
import { getAllDevices } from '$lib/api/devices';
import type { Device } from '$lib/api/model/device';

export const load: PageLoad = async ({ fetch }) => {
	let devices_ = await getAllDevices(fetch)
		.catch(error => {
			console.log('getAllDevices failed:', error);
		});

	let devices: Device[] = devices_ || [];
	return { devices };
};
