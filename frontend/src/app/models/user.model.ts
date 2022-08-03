export interface User {
    id: string,
    aggregateVersion: number,
    firstName: string,
    lastName: string,
    wikiName: string,
    locked: boolean,
    notes: string | null,
    memberQualifications: Set<string>,
    instructorQualifications: Set<string> | null,
    isAdmin: boolean
}
