import { UserSortModel } from "./fabx-state";
import { UserCreationDetails, UserDetails } from "../models/user.model";

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

    export class ChangePersonalInformation {
        static readonly type = "[fabX Users] Change Personal Information"

        constructor(public userId: string, public details: UserDetails) {}
    }

    export class AddMemberQualification {
        static readonly type = "[fabX Users] Add Member Qualification"

        constructor(public userId: string, public qualificationId: string) {}
    }

    export class RemoveMemberQualification {
        static readonly type = "[fabX Users] Remove Member Qualification"

        constructor(public userId: string, public qualificationId: string) {}
    }

    export class AddInstructorQualification {
        static readonly type = "[fabX Users] Add Instructor Qualification"

        constructor(public userId: string, public qualificationId: string) {}
    }

    export class RemoveInstructorQualification {
        static readonly type = "[fabX Users] Remove Instructor Qualification"

        constructor(public userId: string, public qualificationId: string) {}
    }
}
