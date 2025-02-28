export interface FabXError {
	type: string,
	message: string,
	parameters: Record<string, string>,
	correlationId: string | null
}

export class UnauthorizedError extends Error {
	kind: string;

	constructor() {
		const kind = 'Unauthorized';
		super(kind);
		this.kind = kind;
	}
}