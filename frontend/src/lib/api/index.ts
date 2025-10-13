export const baseUrl = '/api/v1';

export type FetchFunction = {
	(input: RequestInfo | URL, init?: RequestInit): Promise<Response>;
	(input: RequestInfo | URL, init?: RequestInit): Promise<Response>;
};
