import { UserSortModel } from "./fabx-state";
import { UserCreationDetails } from "../models/user.model";

export namespace Users {
    export class GetAll {
        static readonly type = "[fabX Users] Get All"
    }

    export class GetById {
        static readonly type = "[fabX Users] Get By Id"

        constructor(public id: string) {}
    }

    export class SetSort {
        static readonly type = "[fabX Users] Set Sort"

        constructor(public sort: UserSortModel) {}
    }

    export class Add {
        static readonly type = "[fabX Users] Add"

        constructor(public details: UserCreationDetails) {}
    }
}
