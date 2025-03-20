import type { LayoutLoad } from './$types';
import { getMe } from '$lib/api/users';
import { redirect } from '@sveltejs/kit';
import { UNAUTHORIZED_ERROR } from '$lib/api/model/error';

export const load: LayoutLoad = async ({ fetch }) => {
	try {
		const me = await getMe(fetch);
		return { me };
	} catch (error) {
		if (error === UNAUTHORIZED_ERROR) {
			redirect(302, '/login');
		}
	}
	throw Error('Runtime exception. Should never reach.');
};
