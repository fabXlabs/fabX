import type { PageLoad } from './$types';
import { getDeviceById } from '$lib/api/devices';

export const load: PageLoad = async ({ fetch, params }) => {
	const device = await getDeviceById(fetch, params.id).catch((error) => {
		console.log('getDeviceById failed:', error);
		return null;
	});
	return { device };
};
