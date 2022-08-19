import { UserSortModel } from "./fabx-state";
import {
    CardIdentityAdditionDetails,
    PhoneNrIdentityAdditionDetails,
    UserCreationDetails,
    UserDetails,
    UserLockDetails,
    UsernamePasswordIdentityAdditionDetails
} from "../models/user.model";

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

    export class ChangeLockState {
        static readonly type = "[fabX Users] Change Lock State"

        constructor(public userId: string, public details: UserLockDetails) {}
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

    export class ChangeIsAdmin {
        static readonly type = "[fabX Users] Change Is Admin"

        constructor(public userId: string, public isAdmin: boolean) {}
    }

    export class AddUsernamePasswordIdentity {
        static readonly type = "[fabX Users] Add Username/Password Identity"

        constructor(public userId: string, public details: UsernamePasswordIdentityAdditionDetails) {}
    }

    export class RemoveUsernamePasswordIdentity {
        static readonly type = "[fabX Users] Remove Username/Password Identity"

        constructor(public userId: string, public username: string) {}
    }

    export class AddWebauthnIdentity {
        static readonly type = "[fabX Users] Add Webauthn Identity"

        constructor(public userId: string) {}
    }

    export class RemoveWebauthnIdentity {
        static readonly type = "[fabX Users] Remove Webauthn Identity"

        constructor(public userId: string, public credentialId: number[]) {}
    }

    export class AddCardIdentity {
        static readonly type = "[fabX Users] Add Card Identity"

        constructor(public userId: string, public details: CardIdentityAdditionDetails) {}
    }

    export class RemoveCardIdentity {
        static readonly type = "[fabX Users] Remove Card Identity"

        constructor(public userId: string, public cardId: string) {}
    }

    export class AddPhoneNrIdentity {
        static readonly type = "[fabX Users] Add Phone Number Identity"

        constructor(public userId: string, public details: PhoneNrIdentityAdditionDetails) {}
    }

    export class RemovePhoneNrIdentity {
        static readonly type = "[fabX Users] Remove Phone Number Identity"

        constructor(public userId: string, public phoneNr: string) {}
    }

    export class Delete {
        static readonly type = "[fabX Users] Delete"

        constructor(public userId: string) {}
    }
}
