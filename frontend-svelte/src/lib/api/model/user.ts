import type { ChangeableValue } from './changeable-value';
import type { Qualification } from '$lib/api/model/qualification';

export interface User {
	id: string;
	aggregateVersion: number;
	firstName: string;
	lastName: string;
	wikiName: string;
	locked: boolean;
	notes: string | null;
	identities: UserIdentity[];
	memberQualifications: string[];
	instructorQualifications: string[] | null;
	isAdmin: boolean;
}

export interface AugmentedUser {
	id: string;
	aggregateVersion: number;
	firstName: string;
	lastName: string;
	wikiName: string;
	locked: boolean;
	notes: string | null;
	identities: UserIdentity[];
	memberQualifications: Qualification[];
	instructorQualifications: Qualification[] | null;
	isAdmin: boolean;
}

export interface UserCreationDetails {
	firstName: string;
	lastName: string;
	wikiName: string;
}

export interface UserDetails {
	firstName: ChangeableValue<string> | null;
	lastName: ChangeableValue<string> | null;
	wikiName: ChangeableValue<string> | null;
}

export interface UserLockDetails {
	locked: ChangeableValue<boolean> | null;
	notes: ChangeableValue<string | null> | null;
}

export interface QualificationAdditionDetails {
	qualificationId: string;
}

export interface IsAdminDetails {
	isAdmin: boolean;
}

export interface UsernamePasswordIdentityAdditionDetails {
	username: string;
	password: string;
}

export interface ChangePasswordDetails {
	password: string;
}

export interface UsernamePasswordIdentity {
	type: 'cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity';
	username: string;
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

export interface CardIdentityAdditionDetails {
	cardId: string;
	cardSecret: string;
}

export interface CardIdentity {
	type: 'cloud.fabX.fabXaccess.user.rest.CardIdentity';
	cardId: string;
	cardSecret: string;
}

export interface PhoneNrIdentityAdditionDetails {
	phoneNr: string;
}

export interface PhoneNrIdentity {
	type: 'cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity';
	phoneNr: string;
}

export interface PinIdentityAdditionDetails {
	pin: string;
}

export interface PinIdentity {
	type: 'cloud.fabX.fabXaccess.user.rest.PinIdentity';
}

export interface WebauthnIdentity {
	type: 'cloud.fabX.fabXaccess.user.rest.WebauthnIdentity';
	credentialId: Int8Array;
}

export type UserIdentity =
	| UsernamePasswordIdentity
	| CardIdentity
	| PhoneNrIdentity
	| PinIdentity
	| WebauthnIdentity;

export interface TokenResponse {
	token: string;
}

export interface UserSourcingEvent {
	type: string;
	aggregateRootId: string;
	aggregateVersion: number;
	actorId: ActorId;
	correlationId: string;
	timestamp: string;
}

export interface SystemActorId {
	type: 'cloud.fabX.fabXaccess.common.model.SystemActorId';
}

export interface DeviceActorId {
	type: 'cloud.fabX.fabXaccess.common.model.DeviceId';
	value: string;
}

export interface UserActorId {
	type: 'cloud.fabX.fabXaccess.common.model.UserId';
	value: string;
}

export type ActorId = SystemActorId | DeviceActorId | UserActorId;
