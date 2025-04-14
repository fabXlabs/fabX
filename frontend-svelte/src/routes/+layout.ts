import type { LayoutLoad } from "./$types";
import { getMe } from "$lib/api/users";
import { redirect } from '@sveltejs/kit';
import { UNAUTHORIZED_ERROR, type FabXError } from '$lib/api/model/error';
import { base } from '$app/paths';
import { error } from '@sveltejs/kit';

export const ssr = true;

export const load: LayoutLoad  = async ({ url, fetch, }) => {
    if (url.pathname === `${base}/login`) {
		// no redirect if user already on login page
        return {};
    }
    try {
		const me = await getMe(fetch);
		return { me, meError: null };
	} catch (e) {
		if (e === UNAUTHORIZED_ERROR) {
			redirect(302, `${base}/login`);
		}
		// if error is other than UNAUTHORIZED_ERROR, backend is in unexpected state
		error(500, "Unexpected server response while attempting to fetch user data. Backend responded with error: " + (e as FabXError))
	}
};