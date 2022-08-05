import { Injectable } from "@angular/core";
import { Action, Selector, State, StateContext } from "@ngxs/store";
import { UserService } from "../services/user.service";
import { User, UserVM } from "../models/user.model";
import { Users } from "./user.actions";
import { tap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { getFinishedValueOrDefault, LoadingState, LoadingStateTag } from "./loading-state.model";
import { AuthService } from "../services/auth.service";
import { Auth } from "./auth.actions";
import { Navigate, RouterState, RouterStateModel } from "@ngxs/router-plugin";
import { Qualification } from "../models/qualification.model";
import { Qualifications } from "./qualification.action";
import { QualificationService } from "../services/qualification.service";

export interface AuthModel {
    username: string,
    password: string,
}

export interface UserSortModel {
    by: keyof User,
    order: "ascending" | "descending",
}

export interface FabxStateModel {
    auth: AuthModel | null,
    loggedInUserId: string | null,
    users: LoadingState<User[]>,
    usersSort: UserSortModel,
    qualifications: LoadingState<Qualification[]>
}

@State<FabxStateModel>({
    name: 'fabx',
    defaults: {
        auth: null,
        loggedInUserId: null,
        users: { tag: "LOADING" },
        usersSort: {
            by: "isAdmin",
            order: "descending",
        },
        qualifications: { tag: "LOADING" }
    }
})
@Injectable()
export class FabxState {
    constructor(
        private authService: AuthService,
        private userService: UserService,
        private qualificationService: QualificationService
    ) {}

    // AUTH

    @Selector()
    static auth(state: FabxStateModel): AuthModel | null {
        return state.auth;
    }

    @Selector()
    static isAuthenticated(state: FabxStateModel): boolean {
        return Boolean(state.auth);
    }

    @Action(Auth.Login)
    login(ctx: StateContext<FabxStateModel>, action: Auth.Login) {
        return this.authService.login(action.payload.username, action.payload.password).pipe(
            tap({
                next: loggedInUser => {
                    ctx.patchState({
                        auth: { username: action.payload.username, password: action.payload.password },
                        loggedInUserId: loggedInUser.id
                    });
                }
            })
        );
    }

    @Action(Auth.Logout)
    logout(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            auth: null,
            loggedInUserId: null
        });
    }

    // USERS

    @Selector()
    static usersLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.users.tag;
    }


    @Selector()
    static users(state: FabxStateModel): UserVM[] {
        let qualifications: Qualification[] = [...getFinishedValueOrDefault(state.qualifications, [])];
        let users: UserVM[] = [...getFinishedValueOrDefault(state.users, [])]
            .map(user => this.augmentUserWithQualifications(user, qualifications));

        let orderMultiplier = state.usersSort.order == "ascending" ? 1 : -1;

        users.sort((a: UserVM, b: UserVM) => {
            let aVal = a[state.usersSort.by] || false;
            let bVal = b[state.usersSort.by] || false;

            if (typeof aVal === "string") {
                aVal = aVal.toLowerCase();
            }
            if (typeof bVal === "string") {
                aVal = bVal.toLowerCase();
            }

            let ret: number = 0;

            if (aVal < bVal) {
                ret = -1;
            }
            if (aVal > bVal) {
                ret = 1;
            }

            return ret * orderMultiplier;
        });

        return users;
    }

    @Selector([RouterState])
    static selectedUser(state: FabxStateModel, router: RouterStateModel): UserVM | null {
        const id = router.state?.root.firstChild?.params['id'];

        const qualifications: Qualification[] = [...getFinishedValueOrDefault(state.qualifications, [])];

        if (state.users.tag == "FINISHED" && id) {
            const user = state.users.value.find(user => user.id == id);
            if (user) {
                return this.augmentUserWithQualifications(user, qualifications);
            }
        }
        return null;
    }

    private static augmentUserWithQualifications(user: User, qualifications: Qualification[]): UserVM {
        const memberQualifications = user.memberQualifications
            .map(qualificationId => qualifications.find(qualification => qualification.id == qualificationId))
            .filter((q): q is Qualification => !!q)

        let instructorQualifications: Qualification[] = [];
        if (user.instructorQualifications) {
            instructorQualifications = user.instructorQualifications
                .map(qualificationId => qualifications.find(qualification => qualification.id == qualificationId))
                .filter((q): q is Qualification => !!q);
        }

        return {
            ...user,
            memberQualifications: memberQualifications,
            instructorQualifications: instructorQualifications
        };
    }

    @Selector()
    static loggedInUser(state: FabxStateModel): User | null {
        if (state.users.tag == "FINISHED" && state.loggedInUserId) {
            return state.users.value.find(user => {
                return user.id == state.loggedInUserId
            }) || null;
        } else {
            return null;
        }
    }

    @Action(Users.GetAll)
    getAllUsers(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            users: { tag: "LOADING" }
        });

        return this.userService.getAllUsers().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        users: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    console.error("error while getting all users: ", err);
                    ctx.patchState({
                        users: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }

    @Action(Users.SetSort)
    setUserSort(ctx: StateContext<FabxStateModel>, action: Users.SetSort) {
        ctx.patchState({
            usersSort: action.sort
        });
    }

    @Action(Users.GetById)
    getUser(ctx: StateContext<FabxStateModel>, action: Users.GetById) {
        return this.userService.getById(action.id).pipe(
            tap({
                next: value => {
                    const state = ctx.getState();
                    if (state.users.tag == "FINISHED") {
                        ctx.patchState({
                            users: {
                                tag: "FINISHED",
                                value: state.users.value.filter((u) => u.id != action.id).concat([value])
                            }
                        });
                    }
                }
            })
        );
    }

    @Action(Users.Add)
    addUser(ctx: StateContext<FabxStateModel>, action: Users.Add) {
        return this.userService.addUser(action.details).pipe(
            tap({
                next: value => {
                    ctx.dispatch(new Users.GetById(value)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['user', value]));
                        }
                    });
                }
            })
        );
    }

    // QUALIFICATIONS
    @Selector()
    static qualificationsLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.qualifications.tag;
    }

    @Selector()
    static qualifications(state: FabxStateModel): Qualification[] {
        return [...getFinishedValueOrDefault(state.qualifications, [])].sort((a: Qualification, b: Qualification) => {
            return a.orderNr - b.orderNr;
        });
    }

    @Action(Qualifications.GetAll)
    getAllQualifications(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            qualifications: { tag: "LOADING" }
        });

        return this.qualificationService.getAllQualifications().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        qualifications: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    ctx.patchState({
                        qualifications: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }
}
