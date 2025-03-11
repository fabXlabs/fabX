import { baseUrl, type FetchFunction } from '$lib/api';
import { mapError } from '$lib/api/map-error';
import type { AugmentedDevice, Device } from '$lib/api/model/device';
import type { Tool } from '$lib/api/model/tool';

export async function getAllDevices(fetch: FetchFunction): Promise<Device[]> {
	console.debug('getAllDevices...');
	const res = await fetch(`${baseUrl}/device`, { credentials: 'include' })
		.then(mapError);
	return res.json();
}

export function augmentDevices(devices: Device[], tools: Tool[]): AugmentedDevice[] {
	const toolsMap = new Map(tools.map(t => [t.id, t]));

	return devices.map(d => ({
		...d,
		attachedTools: Object.fromEntries(
			Array
				.from(
					Object.entries(d.attachedTools),
					([pin, tool]) => [Number(pin), toolsMap.get(tool)]
				)
				.filter(([_, v]) => v)
		)
	}));
}