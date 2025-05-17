import { type FetchFunction } from '$lib/api';
import type { AugmentedDevice, Device } from '$lib/api/model/device';
import type { Tool } from '$lib/api/model/tool';
import { getRequest } from '$lib/api/common';

export async function getAllDevices(fetch: FetchFunction): Promise<Device[]> {
	console.debug('getAllDevices...');
	return await getRequest(fetch, '/device');
}

export async function getDeviceById(fetch: FetchFunction, id: string): Promise<Device> {
	console.debug(`getDeviceById(${id})...`);
	return await getRequest(fetch, `/device/${id}`);
}

export function augmentDevices(devices: Device[], tools: Tool[]): AugmentedDevice[] {
	const toolsMap = new Map(tools.map((t) => [t.id, t]));

	return devices.map((d) => ({
		...d,
		attachedTools: Object.fromEntries(
			Array.from(Object.entries(d.attachedTools), ([pin, tool]) => [
				Number(pin),
				toolsMap.get(tool)
			]).filter(([, v]) => v)
		)
	}));
}
