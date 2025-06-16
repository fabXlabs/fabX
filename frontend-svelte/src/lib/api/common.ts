/* eslint-disable  @typescript-eslint/no-explicit-any */

import { baseUrl, type FetchFunction } from '$lib/api/index';
import { mapError } from '$lib/api/map-error';

export async function getRequest(fetch: FetchFunction, path: string): Promise<any> {
	const res = await fetch(`${baseUrl}${path}`, { credentials: 'include' }).then(mapError);
	return res.json();
}

export async function putRequest(
	fetch: FetchFunction,
	path: string,
	id: string,
	details: any
): Promise<any> {
	const res = await fetch(`${baseUrl}${path}`, {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(details)
	}).then(mapError);

	if (res.status == 204) {
		return id;
	} else {
		return res.text();
	}
}

export async function deleteRequest(fetch: FetchFunction, path: string, id: string): Promise<any> {
	const res = await fetch(`${baseUrl}${path}`, {
		method: 'DELETE'
	}).then(mapError);

	if (res.status == 204) {
		return id;
	} else {
		return res.text();
	}
}
