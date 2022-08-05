import { QualificationCreationDetails } from "../models/qualification.model";

export namespace Qualifications {
    export class GetAll {
        static readonly type = "[fabX Qualifications] Get All"
    }

    export class GetById {
        static readonly type = "[fabX Qualifications] Get By Id"

        constructor(public id: string) {}
    }

    export class Add {
        static readonly type = "[fabX Qualifications] Add"

        constructor(public details: QualificationCreationDetails) {}
    }
}
