import type { PageLoad } from './$types';
import { getMe } from '$lib/api/users';

export const ssr = true;

export const load: PageLoad = async ({ fetch }) => {
	try {
		const me = await getMe(fetch);
		return { me, error: null };
	} catch (error) {
		return { me: null, error };
	}
};