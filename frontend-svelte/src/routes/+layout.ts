import type { LayoutLoad } from './$types';
import { base } from '$app/paths';
import { getMe } from '$lib/api/users';
import { redirect } from '@sveltejs/kit';
import { UNAUTHORIZED_ERROR } from '$lib/api/model/error';

export const ssr = true;

export const load: LayoutLoad = async ({ fetch }) => {
	try {
		const mePromise = getMe(fetch);
		return { mePromise };
	} catch (error) {
		return { me: null, error };
	}
};
