import { type FabXError, UNAUTHORIZED_ERROR } from '$lib/api/model/error';

export async function mapError(response: Response): Promise<Response> {
	if (response.ok) {
		return response;
	} else if (response.status == 401) {
		throw UNAUTHORIZED_ERROR;
	} else {
		throw (await response.json()) as FabXError;
	}
}
