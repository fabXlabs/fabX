import { Qualification } from "./qualification.model";

export interface User {
    id: string,
    aggregateVersion: number,
    firstName: string,
    lastName: string,
    wikiName: string,
    locked: boolean,
    notes: string | null,
    memberQualifications: string[],
    instructorQualifications: string[] | null,
    isAdmin: boolean
}

export interface UserVM {
    id: string,
    aggregateVersion: number,
    firstName: string,
    lastName: string,
    wikiName: string,
    locked: boolean,
    notes: string | null,
    memberQualifications: Qualification[],
    instructorQualifications: Qualification[] | null,
    isAdmin: boolean
}

export interface UserCreationDetails {
    firstName: string,
    lastName: string,
    wikiName: string
}
