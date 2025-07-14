import { baseUrl, type FetchFunction } from '$lib/api';
import type {
	AugmentedUser,
	CardIdentityAdditionDetails,
	IsAdminDetails,
	PhoneNrIdentityAdditionDetails,
	PinIdentityAdditionDetails,
	QualificationAdditionDetails,
	User,
	UserCreationDetails,
	UserDetails,
	UserLockDetails,
	UsernamePasswordIdentityAdditionDetails,
	UserSourcingEvent
} from '$lib/api/model/user';
import { mapError } from '$lib/api/map-error';
import type { Qualification } from '$lib/api/model/qualification';
import { augmentQualifications } from '$lib/api/model/augment-qualifications';
import { deleteRequest, getRequest, postRequest, putRequest } from '$lib/api/common';
import { validatePassword } from '$lib/api/auth';
import { INVALID_PASSWORD_ERROR } from '$lib/api/model/error';

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

export async function getUserSourcingEventsById(
	fetch: FetchFunction,
	id: string
): Promise<UserSourcingEvent[]> {
	console.debug(`getUserSourcingEventsById(${id})`);
	return await getRequest(fetch, `/user/${id}/sourcing-event`);
}

export async function getSoftDeletedUsers(fetch: FetchFunction): Promise<User[]> {
	console.debug('getSoftDeletedUsers...');
	return await getRequest(fetch, '/user/soft-deleted');
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
	return await postRequest(fetch, '/user', 'unknown', details);
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

	return await postRequest(fetch, `/user/${userId}/identity/webauthn/response`, userId, details);
}

export async function addUsernamePasswordIdentity(
	fetch: FetchFunction,
	userId: string,
	username: string,
	password: string
) {
	if (!validatePassword(password)) {
		throw INVALID_PASSWORD_ERROR;
	}

	const details: UsernamePasswordIdentityAdditionDetails = { username, password };
	return await postRequest(fetch, `/user/${userId}/identity/username-password`, userId, details);
}

export async function removeUsernamePasswordIdentity(
	fetch: FetchFunction,
	userId: string,
	username: string
) {
	return await deleteRequest(
		fetch,
		`/user/${userId}/identity/username-password/${username}`,
		userId
	);
}

export async function addCardIdentity(
	fetch: FetchFunction,
	userId: string,
	cardId: string,
	cardSecret: string
) {
	const details: CardIdentityAdditionDetails = { cardId, cardSecret };
	return await postRequest(fetch, `/user/${userId}/identity/card`, userId, details);
}

export async function removeCardIdentity(fetch: FetchFunction, userId: string, cardId: string) {
	return await deleteRequest(fetch, `/user/${userId}/identity/card/${cardId}`, userId);
}

export async function addPhoneIdentity(fetch: FetchFunction, userId: string, phoneNr: string) {
	const details: PhoneNrIdentityAdditionDetails = { phoneNr };
	return await postRequest(fetch, `/user/${userId}/identity/phone`, userId, details);
}

export async function removePhoneIdentity(fetch: FetchFunction, userId: string, phoneNr: string) {
	return await deleteRequest(fetch, `/user/${userId}/identity/phone/${phoneNr}`, userId);
}

export async function addPinIdentity(fetch: FetchFunction, userId: string, pin: string) {
	const details: PinIdentityAdditionDetails = { pin };
	return await postRequest(fetch, `/user/${userId}/identity/pin`, userId, details);
}

export async function removePinIdentity(fetch: FetchFunction, userId: string) {
	return await deleteRequest(fetch, `/user/${userId}/identity/pin`, userId);
}

export async function removeWebauthnIdentity(
	fetch: FetchFunction,
	userId: string,
	credentialId: string
) {
	return await deleteRequest(fetch, `/user/${userId}/identity/webauthn/${credentialId}`, userId);
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

export async function addMemberQualification(
	fetch: FetchFunction,
	userId: string,
	qualificationId: string
): Promise<string> {
	const details: QualificationAdditionDetails = { qualificationId };
	return await postRequest(fetch, `/user/${userId}/member-qualification`, userId, details);
}

export async function removeMemberQualification(
	fetch: FetchFunction,
	userId: string,
	qualificationId: string
): Promise<string> {
	return await deleteRequest(
		fetch,
		`/user/${userId}/member-qualification/${qualificationId}`,
		userId
	);
}

export async function addInstructorQualification(
	fetch: FetchFunction,
	userId: string,
	qualificationId: string
): Promise<string> {
	const details: QualificationAdditionDetails = { qualificationId };
	return await postRequest(fetch, `/user/${userId}/instructor-qualification`, userId, details);
}

export async function removeInstructorQualification(
	fetch: FetchFunction,
	userId: string,
	qualificationId: string
): Promise<string> {
	return await deleteRequest(
		fetch,
		`/user/${userId}/instructor-qualification/${qualificationId}`,
		userId
	);
}

export async function changeIsAdmin(
	fetch: FetchFunction,
	userId: string,
	isAdmin: boolean
): Promise<string> {
	const details: IsAdminDetails = { isAdmin };
	return await putRequest(fetch, `/user/${userId}/is-admin`, userId, details);
}

export async function deleteUser(fetch: FetchFunction, userId: string): Promise<string> {
	return await deleteRequest(fetch, `/user/${userId}`, userId);
}

export async function hardDeleteUser(fetch: FetchFunction, userId: string): Promise<string> {
	return await deleteRequest(fetch, `/user/soft-deleted/${userId}`, userId);
}
