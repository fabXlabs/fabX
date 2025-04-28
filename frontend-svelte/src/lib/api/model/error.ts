export interface FabXError {
	type: string;
	message: string;
	parameters: Record<string, string>;
	correlationId: string | null;
}

export const UNAUTHORIZED_ERROR: FabXError = {
	type: 'Unauthorized',
	message: 'Unauthorized',
	parameters: {},
	correlationId: null
};
