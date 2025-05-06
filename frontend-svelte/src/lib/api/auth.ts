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

interface WebauthnPasskeyLoginResponse {
	challenge: number[];
}

interface WebauthnPasskeyResponseDetails {
	credentialId: number[];
	authenticatorData: number[];
	clientDataJSON: number[];
	signature: number[];
}

export async function loginWebauthn() {
	// get a global challenge
	const challengeRes = await fetch(`${baseUrl}/webauthn/login/passkey`, {
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		},
		method: 'POST'
	}).then(mapError);

	const loginResponse = (await challengeRes.json()) as WebauthnPasskeyLoginResponse;
	const challengeArray = new Int8Array(loginResponse.challenge);

	// Use conditional mediation to show the passkey selection UI
	const credential = await navigator.credentials.get({
		publicKey: {
			challenge: challengeArray.buffer
		},
		mediation: 'conditional' // This makes the browser show the passkey selection UI
	});

	if (!credential) {
		throw new Error('No passkey provided'); // FIXME: This should be a custom user-friendly message
	}

	const pkc = credential as PublicKeyCredential;
	const r = pkc.response as AuthenticatorAssertionResponse;

	const credentialIdArray = new Int8Array(pkc.rawId);
	const authenticatorDataArray = new Int8Array(r.authenticatorData);
	const clientDataJSONArray = new Int8Array(r.clientDataJSON);
	const signatureArray = new Int8Array(r.signature);

	// No userId - we'll let the server identify the user from the credential
	const response: WebauthnPasskeyResponseDetails = {
		credentialId: Array.from(credentialIdArray),
		authenticatorData: Array.from(authenticatorDataArray),
		clientDataJSON: Array.from(clientDataJSONArray),
		signature: Array.from(signatureArray)
	};

	const res = await fetch(`${baseUrl}/webauthn/passkey/response?cookie=true`, {
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		},
		method: 'POST',
		body: JSON.stringify(response)
	}).then(mapError);

	console.log('passkey login result:', res);
	return res;
}

export async function logout() {
	return await fetch(`${baseUrl}/logout`, { credentials: 'include' }).then(mapError);
}
