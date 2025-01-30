import { baseUrl } from '$lib';
import { deserialize } from '$lib/deserialize';

export interface Qualification {
	id: string,
	aggregateVersion: number,
	name: string,
	description: string,
	colour: string,
	orderNr: number
}

type FetchFunction = {
	(input: (RequestInfo | URL), init?: RequestInit): Promise<Response>;
	(input: (RequestInfo | URL), init?: RequestInit): Promise<Response>
};


export async function getAllQualifications(fetch: FetchFunction): Promise<Qualification[]> {
	console.debug('getQualifications...');

	return await fetch(`${baseUrl}/qualification`, { credentials: 'include' })
		.then(deserialize<Qualification[]>)
		// .then(response => {
		// 	return new Promise((resolve, reject) => {
		// 		if (response.status < 400) {
		// 			resolve(response.json() as Promise<Qualification[]>);
		// 		} else if (response.status == 401) {
		// 			// redirect(302, "/login");
		// 			reject(new UnauthorizedError());
		// 		} else {
		// 			// TODO deserialize backend error type
		// 			reject(new Error(`Cannot get all qualifications: ${response.status} ${response.statusText}`));
		// 		}
		// 	})
		// });
}
