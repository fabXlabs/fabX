import { type FetchFunction } from '$lib/api';
import type { Qualification, QualificationCreationDetails } from '$lib/api/model/qualification';
import { getRequest, postRequest } from '$lib/api/common';

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

export async function addQualification(
	fetch: FetchFunction,
	details: QualificationCreationDetails
): Promise<string> {
	return await postRequest(fetch, '/qualification', 'unknown', details);
}
