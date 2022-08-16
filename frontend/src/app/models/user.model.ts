import { Qualification } from "./qualification.model";
import { ChangeableValue } from "./changeable-value";

export interface User {
    id: string,
    aggregateVersion: number,
    firstName: string,
    lastName: string,
    wikiName: string,
    locked: boolean,
    notes: string | null,
    identities: UserIdentity[],
    memberQualifications: string[],
    instructorQualifications: string[] | null,
    isAdmin: boolean
}

export interface AugmentedUser {
    id: string,
    aggregateVersion: number,
    firstName: string,
    lastName: string,
    wikiName: string,
    locked: boolean,
    notes: string | null,
    identities: UserIdentity[],
    memberQualifications: Qualification[],
    instructorQualifications: Qualification[] | null,
    isAdmin: boolean
}

export interface UserCreationDetails {
    firstName: string,
    lastName: string,
    wikiName: string
}

export interface UserDetails {
    firstName: ChangeableValue<string> | null,
    lastName: ChangeableValue<string> | null,
    wikiName: ChangeableValue<string> | null
}

export interface UserLockDetails {
    locked: ChangeableValue<boolean> | null
    notes: ChangeableValue<string | null> | null
}

export interface QualificationAdditionDetails {
    qualificationId: string
}

export interface IsAdminDetails {
    isAdmin: boolean
}

export interface UsernamePasswordIdentityAdditionDetails {
    username: string,
    password: string
}

export interface UsernamePasswordIdentity {
    type: "cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity",
    username: string
}

export interface CardIdentity {
    type: "cloud.fabX.fabXaccess.user.rest.CardIdentity",
    cardId: string,
    cardSecret: string
}

export interface PhoneNrIdentity {
    type: "cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity",
    phoneNr: string
}

export type UserIdentity = UsernamePasswordIdentity | CardIdentity | PhoneNrIdentity
