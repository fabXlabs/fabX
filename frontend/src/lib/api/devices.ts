import { baseUrl, type FetchFunction } from '$lib/api';
import { deserialize } from '$lib/api/deserialize';
import type { Device } from '$lib/api/model/device';

export async function getAllDevices(fetch: FetchFunction): Promise<Device[]> {
	console.debug('getAllDevices...');

	return await fetch(`${baseUrl}/device`, { credentials: 'include' })
		.then(deserialize<Device[]>);
}
