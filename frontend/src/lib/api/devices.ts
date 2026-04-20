import { baseUrl, type FetchFunction } from '$lib/api/index';
import type {
	AtDeviceCardCreationDetails,
	AugmentedDevice,
	DesiredFirmwareVersion,
	Device,
	DeviceCreationDetails,
	DeviceDetails,
	PinStatus,
	ToolAttachmentDetails,
	ToolUnlockDetails
} from '$lib/api/model/device';
import type { Tool } from '$lib/api/model/tool';
import { deleteRequest, getRequest, postRequest, putRequest } from '$lib/api/common';
import { mapError } from '$lib/api/map-error';
import type { UserSourcingEvent } from '$lib/api/model/user';

export async function getAllDevices(fetch: FetchFunction): Promise<Device[]> {
	console.debug('getAllDevices...');
	return await getRequest(fetch, '/device');
}

export async function getDeviceById(fetch: FetchFunction, id: string): Promise<Device> {
	console.debug(`getDeviceById(${id})...`);
	return await getRequest(fetch, `/device/${id}`);
}

export async function getAllDeviceConnectionStatuses(
	fetch: FetchFunction
): Promise<Map<string, boolean>> {
	console.debug('getAllDeviceConnectionStatuses...');
	return await getRequest(fetch, `/device/connection-status`).then(
		(v) => new Map(Object.entries(v))
	);
}

export async function getDeviceConnectionStatusById(
	fetch: FetchFunction,
	id: string
): Promise<boolean> {
	console.debug('getDeviceConnectionStatusById...');
	return await getRequest(fetch, `/device/${id}/connection-status`);
}

export async function getAllDevicePinStatuses(
	fetch: FetchFunction
): Promise<Map<string, PinStatus>> {
	const r = await getRequest(fetch, `/device/pin-status`);

	return new Map(
		Object.entries(r).map(([id, pinStatus]) => {
			const ps: PinStatus = {
				inputPinStatus: new Map(Object.entries((pinStatus as PinStatus).inputPinStatus)),
				updatedAt: formatTimestamp(pinStatus as PinStatus)
			};

			return [id, ps];
		})
	);
}

export async function getDevicePinStatusById(fetch: FetchFunction, id: string): Promise<PinStatus> {
	console.debug('getDevicePinStatusById...');
	return await getRequest(fetch, `/device/${id}/pin-status`)
		.then((r) => {
			return {
				inputPinStatus: new Map(Object.entries((r as PinStatus).inputPinStatus)),
				updatedAt: formatTimestamp(r as PinStatus)
			};
		})
		.catch((_) => {
			return {
				inputPinStatus: new Map(),
				updatedAt: ''
			};
		});
}

function formatTimestamp(ps: PinStatus): string {
	const ts = new Date(Date.parse(ps.updatedAt));
	return ts.toISOString().replace(/T/, ' ').slice(0, 16);
}

export function deviceThumbnailUrl(id: string): string {
	return `${baseUrl}/device/${id}/thumbnail`;
}

function augmentDevice_(
	device: Device,
	toolsMap: Map<string, Tool>,
	connectionStatuses: Map<string, boolean>,
	pinStatuses: Map<string, PinStatus>
): AugmentedDevice {
	let connectionStatus: boolean | null = null;
	if (connectionStatuses.has(device.id)) {
		connectionStatus = connectionStatuses.get(device.id)!;
	}

	let pinStatus: PinStatus | null = null;
	if (pinStatuses.has(device.id)) {
		pinStatus = pinStatuses.get(device.id)!;
	}

	return {
		...device,
		attachedTools: Object.fromEntries(
			Array.from(Object.entries(device.attachedTools), ([pin, tool]) => [
				Number(pin),
				toolsMap.get(tool)
			]).filter(([, v]) => v)
		),
		connectionStatus,
		pinStatus
	};
}

export function augmentDevice(
	device: Device,
	tools: Tool[],
	connectionStatus: boolean | null,
	pinStatus: PinStatus
): AugmentedDevice {
	const toolsMap = new Map(tools.map((t) => [t.id, t]));

	const connectionStatuses = new Map<string, boolean>();
	if (connectionStatus !== null) {
		connectionStatuses.set(device.id, connectionStatus);
	}

	const pinStatuses = new Map<string, PinStatus>();
	pinStatuses.set(device.id, pinStatus);

	return augmentDevice_(device, toolsMap, connectionStatuses, pinStatuses);
}

export function augmentDevices(
	devices: Device[],
	tools: Tool[],
	connectionStatuses: Map<string, boolean>,
	pinStatuses: Map<string, PinStatus>
): AugmentedDevice[] {
	const toolsMap = new Map(tools.map((t) => [t.id, t]));

	return devices.map((d) => augmentDevice_(d, toolsMap, connectionStatuses, pinStatuses));
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

export async function changeThumbnail(
	fetch: FetchFunction,
	id: string,
	file: File
): Promise<string> {
	const res = await fetch(`${baseUrl}/device/${id}/thumbnail`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: file
	}).then(mapError);

	if (res.status == 204) {
		return id;
	} else {
		return res.text();
	}
}

export async function deleteDevice(fetch: FetchFunction, id: string): Promise<string> {
	return await deleteRequest(fetch, `/device/${id}`, id);
}
