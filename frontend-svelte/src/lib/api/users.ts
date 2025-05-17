import { baseUrl, type FetchFunction } from '$lib/api';
import type {
	AugmentedUser,
	User,
	UserCreationDetails,
	UserDetails,
	UserLockDetails
} from '$lib/api/model/user';
import { mapError } from '$lib/api/map-error';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';
import { getRequest, putRequest } from '$lib/api/common';

export async function getMe(fetch: FetchFunction): Promise<User> {
	console.debug('getMe...');
	return await getRequest(fetch, '/user/me');
}

export async function getAllUsers(fetch: FetchFunction): Promise<User[]> {
	console.debug('getAllUsers...');
	return await getRequest(fetch, '/user');
}

export async function getUserById(fetch: FetchFunction, id: string): Promise<User> {
	console.debug(`getUserById(${id})`);
	return await getRequest(fetch, `/user/${id}`);
}

export function augmentUser(user: User, qualifications: Qualification[]): AugmentedUser {
	const getQualifications = augmentQualifications(qualifications);
	return {
		...user,
		memberQualifications: getQualifications(user.memberQualifications),
		instructorQualifications: getQualifications(user.instructorQualifications || [])
	};
}

export function augmentUsers(users: User[], qualifications: Qualification[]): AugmentedUser[] {
	const getQualifications = augmentQualifications(qualifications);

	return users.map((u) => ({
		...u,
		memberQualifications: getQualifications(u.memberQualifications),
		instructorQualifications: getQualifications(u.instructorQualifications || [])
	}));
}

export async function addUser(details: UserCreationDetails): Promise<string> {
	const res = await fetch(`${baseUrl}/user`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(details)
	}).then(mapError);
	return res.text();
}

export async function changePersonalInformation(id: string, details: UserDetails): Promise<string> {
	return await putRequest(fetch, `/user/${id}`, id, details);
}

export async function changeLockState(id: string, details: UserLockDetails): Promise<string> {
	return await putRequest(fetch, `/user/${id}/lock`, id, details);
}

export async function addWebauthnIdentity(userId: string) {
	const registrationRes = await fetch(`${baseUrl}/user/${userId}/identity/webauthn/register`, {
		method: 'POST'
	}).then(mapError);

	const registrationDetails: WebauthnRegistrationDetails = await registrationRes.json();

	const challengeArray = new Int8Array(registrationDetails.challenge);
	const userIdArray = new Int8Array(registrationDetails.userId);

	const options: CredentialCreationOptions = {
		publicKey: {
			attestation: registrationDetails.attestation,
			challenge: challengeArray.buffer,
			rp: {
				id: registrationDetails.rpId,
				name: registrationDetails.rpName
			},
			user: {
				id: userIdArray.buffer,
				name: registrationDetails.userName,
				displayName: registrationDetails.userDisplayName
			},
			pubKeyCredParams: registrationDetails.pubKeyCredParams
		}
	};

	const credential = await navigator.credentials.create(options);

	if (!credential) {
		throw new Error('Not able to create credential');
	}

	const pkc = credential as PublicKeyCredential;
	const r = pkc.response as AuthenticatorAttestationResponse;

	const attestationArray = Array.from(new Int8Array(r.attestationObject));
	const clientDataArray = Array.from(new Int8Array(pkc.response.clientDataJSON));

	const details: WebauthnIdentityAdditionDetails = {
		attestationObject: attestationArray,
		clientDataJSON: clientDataArray
	};

	await fetch(`${baseUrl}/user/${userId}/identity/webauthn/response`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(details)
	}).then(mapError);
}

export interface WebauthnRegistrationDetails {
	attestation: AttestationConveyancePreference;
	challenge: number[];
	rpName: string;
	rpId: string;
	userId: number[];
	userName: string;
	userDisplayName: string;
	pubKeyCredParams: PublicKeyCredentialParameters[];
}

export interface WebauthnIdentityAdditionDetails {
	attestationObject: number[];
	clientDataJSON: number[];
}
