import { type FetchFunction } from '$lib/api';
import type {
	AtDeviceCardCreationDetails,
	AugmentedDevice,
	Device,
	DeviceCreationDetails,
	DeviceDetails
} from '$lib/api/model/device';
import type { Tool } from '$lib/api/model/tool';
import { deleteRequest, getRequest, postRequest, putRequest } from '$lib/api/common';

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

export async function addDevice(
	fetch: FetchFunction,
	details: DeviceCreationDetails
): Promise<string> {
	return await postRequest(fetch, '/device', 'unknown', details);
}

export async function addCardIdentityAtDevice(fetch: FetchFunction, id: string, userId: string) {
	const details: AtDeviceCardCreationDetails = {
		userId: userId
	};
	return await postRequest(fetch, `/device/${id}/add-user-card-identity`, id, details);
}

export async function changeDeviceDetails(
	fetch: FetchFunction,
	id: string,
	details: DeviceDetails
): Promise<string> {
	return await putRequest(fetch, `/device/${id}`, id, details);
}

export async function deleteDevice(fetch: FetchFunction, id: string): Promise<string> {
	return await deleteRequest(fetch, `/device/${id}`, id);
}
