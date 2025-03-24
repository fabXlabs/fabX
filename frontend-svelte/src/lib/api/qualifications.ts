import { baseUrl, type FetchFunction } from '$lib/api';
import { mapError } from '$lib/api/map-error';
import type { Qualification } from '$lib/api/model/qualification';

export async function getAllQualifications(fetch: FetchFunction): Promise<Qualification[]> {
	console.debug('getAllQualifications...');

	const res = await fetch(`${baseUrl}/qualification`, { credentials: 'include' })
		.then(mapError);
	return res.json();
}

export async function getQualificationById(fetch: FetchFunction, id: string): Promise<Qualification> {
	console.debug(`getQualificationById(${id})...`);

	const res = await fetch(`${baseUrl}/qualification/${id}`, { credentials: 'include' })
		.then(mapError);
	return res.json();
}
