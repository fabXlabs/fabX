import { type FetchFunction } from '$lib/api';
import type { AtDeviceCardCreationDetails, AugmentedDevice, Device } from '$lib/api/model/device';
import type { Tool } from '$lib/api/model/tool';
import { getRequest, postRequest } from '$lib/api/common';

export async function getAllDevices(fetch: FetchFunction): Promise<Device[]> {
	console.debug('getAllDevices...');
	return await getRequest(fetch, '/device');
}

export async function getDeviceById(fetch: FetchFunction, id: string): Promise<Device> {
	console.debug(`getDeviceById(${id})...`);
	return await getRequest(fetch, `/device/${id}`);
}

function augmentDevice_(device: Device, toolsMap: Map<string, Tool>): AugmentedDevice {
	return {
		...device,
		attachedTools: Object.fromEntries(
			Array.from(Object.entries(device.attachedTools), ([pin, tool]) => [
				Number(pin),
				toolsMap.get(tool)
			]).filter(([, v]) => v)
		)
	};
}

export function augmentDevice(device: Device, tools: Tool[]): AugmentedDevice {
	const toolsMap = new Map(tools.map((t) => [t.id, t]));
	return augmentDevice_(device, toolsMap);
}

export function augmentDevices(devices: Device[], tools: Tool[]): AugmentedDevice[] {
	const toolsMap = new Map(tools.map((t) => [t.id, t]));

	return devices.map((d) => augmentDevice_(d, toolsMap));
}

export async function addCardIdentityAtDevice(fetch: FetchFunction, id: string, userId: string) {
	const details: AtDeviceCardCreationDetails = {
		userId: userId
	};
	return await postRequest(fetch, `/device/${id}/add-user-card-identity`, id, details);
}
