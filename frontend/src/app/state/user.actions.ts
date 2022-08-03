import { UserSortModel } from "./fabx-state";

export namespace Users {
    export class GetAll {
        static readonly type = "[fabX Users] Get All"
    }

    export class SetSort {
        static readonly type = "[fabX Users] Set Sort"
        constructor(public sort: UserSortModel) {}
    }
}
