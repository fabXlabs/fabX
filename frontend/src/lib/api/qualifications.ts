import { baseUrl, type FetchFunction } from '$lib/api';
import { deserialize } from '$lib/api/deserialize';
import type { Qualification } from '$lib/api/model/qualification';

export async function getAllQualifications(fetch: FetchFunction): Promise<Qualification[]> {
	console.debug('getAllQualifications...');

	return await fetch(`${baseUrl}/qualification`, { credentials: 'include' })
		.then(deserialize<Qualification[]>);
}
