import { baseUrl, type FetchFunction } from '$lib/api';
import type { Tool } from '$lib/api/model/tool';
import { deserialize } from '$lib/api/deserialize';

export async function getAllTools(fetch: FetchFunction): Promise<Tool[]> {
	console.debug('getAllTools...');

	return await fetch(`${baseUrl}/tool`, { credentials: 'include' })
		.then(deserialize<Tool[]>);
}
