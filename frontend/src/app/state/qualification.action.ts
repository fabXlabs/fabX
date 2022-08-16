import { QualificationCreationDetails, QualificationDetails } from "../models/qualification.model";

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

    export class ChangeDetails {
        static readonly type = "[fabX Qualifications] Change Details"

        constructor(public id: string, public details: QualificationDetails) {}
    }

    export class Delete {
        static readonly type = "[fabX Qualifications] Delete"

        constructor(public id: string) {}
    }
}
