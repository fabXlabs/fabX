import { ToolCreationDetails } from "../models/tool.model";

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
}
