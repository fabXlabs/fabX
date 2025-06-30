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

export const INVALID_PASSWORD_ERROR: FabXError = {
	type: 'InvalidPassword',
	message:
		'Password is not long enough (8 characters min.) or contains forbidden characters (characters 0x21 through 0x7e of the ASCII set are allowed).',
	parameters: {},
	correlationId: null
};
