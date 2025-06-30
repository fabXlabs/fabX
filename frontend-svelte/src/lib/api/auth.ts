import { baseUrl } from '$lib/api';
import { mapError } from '$lib/api/map-error';

export async function loginBasicAuth(username: string, password: string) {
	console.debug(`login...`);

	await logout();

	const headers = {
		Authorization: 'Basic ' + btoa(username + ':' + password)
	};
	const res = await fetch(`${baseUrl}/login?cookie=true`, {
		headers,
		credentials: 'include'
	}).then(mapError);

	console.log('...login result', res);
	return res;
}

export function validatePassword(password: string): boolean {
	const regex = new RegExp('^[!-~]{8,}$');
	return regex.test(password);
}

export async function loginWebauthn(username: string) {
	const challengeRes = await fetch(`${baseUrl}/webauthn/login`, {
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		},
		method: 'POST',
		body: JSON.stringify({
			username: username
		})
	}).then(mapError);

	const loginResponse = (await challengeRes.json()) as WebauthnLoginResponse;

	const userId = loginResponse.userId;

	const allowCred: PublicKeyCredentialDescriptor[] = loginResponse.credentialIds.map((e) => {
		const eArr = new Int8Array(e.values());
		return {
			id: eArr.buffer,
			type: 'public-key'
		};
	});

	const challengeArray = new Int8Array(loginResponse.challenge);

	const credential = await navigator.credentials.get({
		publicKey: {
			allowCredentials: allowCred,
			challenge: challengeArray.buffer
		}
	});

	if (!credential) {
		throw new Error('Webauthn Credential not found');
	}

	const pkc = credential as PublicKeyCredential;
	const r = pkc.response as AuthenticatorAssertionResponse;

	const credentialIdArray = new Int8Array(pkc.rawId);
	const authenticatorDataArray = new Int8Array(r.authenticatorData);
	const clientDataJSONArray = new Int8Array(r.clientDataJSON);
	const signatureArray = new Int8Array(r.signature);

	const response: WebauthnResponseDetails = {
		userId: userId,
		credentialId: Array.from(credentialIdArray),
		authenticatorData: Array.from(authenticatorDataArray),
		clientDataJSON: Array.from(clientDataJSONArray),
		signature: Array.from(signatureArray)
	};

	const res = await fetch(`${baseUrl}/webauthn/response?cookie=true`, {
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		},
		method: 'POST',
		body: JSON.stringify(response)
	}).then(mapError);

	console.log('res', res);
}

export async function logout() {
	return await fetch(`${baseUrl}/logout`, { credentials: 'include' }).then(mapError);
}

interface WebauthnLoginResponse {
	userId: string;
	challenge: number[];
	credentialIds: number[][];
}

interface WebauthnResponseDetails {
	userId: string;
	credentialId: number[];
	authenticatorData: number[];
	clientDataJSON: number[];
	signature: number[];
}
