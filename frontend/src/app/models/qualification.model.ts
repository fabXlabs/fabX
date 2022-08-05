export interface Qualification {
    id: string,
    aggregateVersion: number,
    name: string,
    description: string,
    colour: string,
    orderNr: number
}

export interface QualificationCreationDetails {
    name: string,
    description: string,
    colour: string,
    orderNr: number,
}
