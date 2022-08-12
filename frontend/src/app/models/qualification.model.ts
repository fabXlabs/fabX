import { ChangeableValue } from "./changeable-value";

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

export interface QualificationDetails {
    name: ChangeableValue<string> | null,
    description: ChangeableValue<string> | null,
    colour: ChangeableValue<string> | null,
    orderNr: ChangeableValue<number> | null,
}
