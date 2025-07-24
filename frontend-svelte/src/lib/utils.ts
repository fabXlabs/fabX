import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

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
