import { ToolCreationDetails, ToolDetails } from "../models/tool.model";

export namespace Tools {
    export class GetAll {
        static readonly type = "[fabX Tools] Get All"
    }

    export class GetById {
        static readonly type = "[fabX Tools] Get By Id"

        constructor(public id: string) {}
    }

    export class Add {
        static readonly type = "[fabX Tools] Add"

        constructor(public details: ToolCreationDetails) {}
    }

    export class ChangeDetails {
        static readonly type = "[fabX Tools] Change Details"

        constructor(public id: string, public details: ToolDetails) {}
    }

    export class Delete {
        static readonly type = "[fabX Tools] Delete"

        constructor(public id: string) {}
    }
}
