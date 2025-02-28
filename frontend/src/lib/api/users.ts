import { baseUrl, type FetchFunction } from '$lib/api';
import type { User } from '$lib/api/model/user';
import { deserialize } from '$lib/api/deserialize';

export async function getMe(fetch: FetchFunction): Promise<User> {
	console.debug('getMe...');

	return await fetch(`${baseUrl}/user/me`, { credentials: 'include' })
		.then(deserialize<User>);
}
