import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { browser } from '$app/environment';

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

export function toHexString(arr: Int8Array): string {
	return Array.from(arr, function (byte) {
		return ('0' + (byte & 0xff).toString(16)).slice(-2);
	}).join('');
}

export function areSetsEqual<T>(a: Set<T>, b: Set<T>): boolean {
	return a.size === b.size && [...a].every((value) => b.has(value));
}

const FABX_REDIRECT_AFTER_LOGIN = 'FABX_REDIRECT_AFTER_LOGIN';

export function setRedirectAfterLoginCookie(path: string): void {
	if (browser) {
		// expire in 1 week
		const date = new Date();
		date.setTime(date.getTime() + 7 * 24 * 60 * 60 * 1000);

		document.cookie =
			FABX_REDIRECT_AFTER_LOGIN + '=' + path + '; expires=' + date.toUTCString() + '; path=/';
	} else {
		console.debug('setRedirectAfterLoginCookie not in browser -> not doing anything');
	}
}

export function getRedirectAfterLoginCookie() {
	return document.cookie
		.split('; ')
		.find((row) => row.startsWith(FABX_REDIRECT_AFTER_LOGIN + '='))
		?.split('=')[1];
}

export function deleteRedirectAfterLoginCookie() {
	// expire in -1 days
	const date = new Date();
	date.setTime(date.getTime() + -1 * 24 * 60 * 60 * 1000);

	document.cookie = FABX_REDIRECT_AFTER_LOGIN + '=; expires=' + date.toUTCString() + '; path=/';
}
