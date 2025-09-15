import { type FetchFunction } from '$lib/api';
import type {
	Qualification,
	QualificationCreationDetails,
	QualificationDetails
} from '$lib/api/model/qualification';
import { deleteRequest, getRequest, postRequest, putRequest } from '$lib/api/common';

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

export async function changeQualificationDetails(
	fetch: FetchFunction,
	id: string,
	details: QualificationDetails
): Promise<string> {
	return await putRequest(fetch, `/qualification/${id}`, id, details);
}

export async function deleteQualification(fetch: FetchFunction, id: string): Promise<string> {
	return await deleteRequest(fetch, `/qualification/${id}`, id);
}
