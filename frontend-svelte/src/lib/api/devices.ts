import { baseUrl, type FetchFunction } from '$lib/api/index';
import type {
	AtDeviceCardCreationDetails,
	AugmentedDevice,
	DesiredFirmwareVersion,
	Device,
	DeviceCreationDetails,
	DeviceDetails,
	ToolAttachmentDetails,
	ToolUnlockDetails
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

export function deviceThumbnailUrl(id: string): string {
	return `${baseUrl}/device/${id}/thumbnail`;
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

export async function changeDesiredFirmwareVersion(
	fetch: FetchFunction,
	id: string,
	details: DesiredFirmwareVersion
): Promise<string> {
	return await putRequest(fetch, `/device/${id}/desired-firmware-version`, id, details);
}

export async function attachTool(
	fetch: FetchFunction,
	id: string,
	pin: number,
	toolId: string
): Promise<string> {
	const details: ToolAttachmentDetails = { toolId };
	return await putRequest(fetch, `/device/${id}/attached-tool/${pin}`, id, details);
}

export async function detachTool(fetch: FetchFunction, id: string, pin: number): Promise<string> {
	return await deleteRequest(fetch, `/device/${id}/attached-tool/${pin}`, id);
}

export async function unlockTool(
	fetch: FetchFunction,
	id: string,
	toolId: string
): Promise<string> {
	const details: ToolUnlockDetails = { toolId };
	return await postRequest(fetch, `/device/${id}/unlock-tool`, id, details);
}

export async function restartDevice(fetch: FetchFunction, id: string): Promise<string> {
	const details = {};
	return await postRequest(fetch, `/device/${id}/restart`, id, details);
}

export async function updateDeviceFirmware(fetch: FetchFunction, id: string): Promise<string> {
	const details = {};
	return await postRequest(fetch, `/device/${id}/update-firmware`, id, details);
}

export async function deleteDevice(fetch: FetchFunction, id: string): Promise<string> {
	return await deleteRequest(fetch, `/device/${id}`, id);
}
