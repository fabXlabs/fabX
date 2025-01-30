export class UnauthorizedError extends Error {
	kind: string;

	constructor() {
		const kind = 'Unauthorized';
		super(kind);
		this.kind = kind;
	}
}

export function deserialize<T>(response: Response): Promise<T> {
	return new Promise((resolve, reject) => {
		if (response.status < 400) {
			resolve(response.json() as Promise<T>);
		} else if (response.status == 401) {
			reject(new UnauthorizedError());
		} else {
			// TODO deserialize backend error type
			reject(new Error(`Error from backend: ${response.status} ${response.statusText}`));
		}
	});
}

export function checkError(response: Response): Promise<Response> {
	return new Promise((resolve, reject) => {
		if (response.status == 401) {
			reject(new UnauthorizedError());
		} else if (response.status >= 400) {
			reject(new Error(`Error from backend: ${response.status} ${response.statusText}`));
		} else {
			resolve(response);
		}
	});
}