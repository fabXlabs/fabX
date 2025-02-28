import { baseUrl } from '$lib/api';
import { checkError } from '$lib/api/deserialize';

export async function loginBasicAuth(username: string, password: string) {
	console.debug(`login...`);

	await logout();

	const headers = {
		'Authorization': 'Basic ' + btoa(username + ':' + password)
	};
	const res = await fetch(`${baseUrl}/login?cookie=true`, { headers, credentials: 'include' })
		.then(checkError);

	console.log('...login result', res);
	return res;
}

export async function logout() {
	await fetch(`${baseUrl}/logout`, { credentials: 'include' });
}
