import type { LayoutLoad } from './$types';
import { getMe } from '$lib/api/users';
import { UnauthorizedError } from '$lib/api/model/error';
import { redirect } from '@sveltejs/kit';

export const load: LayoutLoad = async ({ fetch }) => {
	let me = await getMe(fetch)
		.catch(error => {
			console.log('getMe failed:', error);
			if (error instanceof UnauthorizedError) {
				redirect(302, '/login')
			}
		})

	return { me };
};
