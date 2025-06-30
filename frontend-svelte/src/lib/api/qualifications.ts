import { type FetchFunction } from '$lib/api';
import type { Qualification } from '$lib/api/model/qualification';
import { getRequest } from '$lib/api/common';

export async function getAllQualifications(fetch: FetchFunction): Promise<Qualification[]> {
	console.debug('getAllQualifications...');
	return await getRequest(fetch, '/qualification');
}

export async function getQualificationById(
	fetch: FetchFunction,
	id: string
): Promise<Qualification> {
	console.debug(`getQualificationById(${id})...`);
	return await getRequest(fetch, `/qualification/${id}`);
}
