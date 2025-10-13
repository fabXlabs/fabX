import { getContext, setContext } from 'svelte';
import type { User } from '$lib/api/model/user';

const key = 'me';

export function setMeContext(user: User) {
	setContext(key, user);
}

export function getMeContext() {
	return getContext(key) as User;
}
