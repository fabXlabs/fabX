import type { LayoutLoad } from "./$types";
import { getMe } from "$lib/api/users";
import { redirect } from '@sveltejs/kit';
import { UNAUTHORIZED_ERROR } from '$lib/api/model/error';
import { base } from '$app/paths';

export const ssr = true;

export const load: LayoutLoad  = async ({ url, fetch, }) => {
	// check if user  on path /login
    if (url.pathname === `${base}/login`) {
        return {};
    }
    try {
		const me = await getMe(fetch);
		return { me, meError: null };
	} catch (error) {
		if (error === UNAUTHORIZED_ERROR) {
			redirect(302, `${base}/login`);
		}
		return { me: null, meError: error };
	}
};