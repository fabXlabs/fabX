import { Injectable } from "@angular/core";
import { Action, Selector, State, StateContext } from "@ngxs/store";
import { UserService } from "../services/user.service";
import { User } from "../models/user.model";
import { Users } from "./user.actions";
import { tap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { getFinishedValueOrDefault, LoadingState, LoadingStateTag } from "./loading-state.model";
import { AuthService } from "../services/auth.service";
import { Auth } from "./auth.actions";
import { RouterState, RouterStateModel } from "@ngxs/router-plugin";

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
        }
    }
})
@Injectable()
export class FabxState {
    constructor(private authService: AuthService, private userService: UserService) {}

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
    static users(state: FabxStateModel): User[] {
        let users: User[] = [...getFinishedValueOrDefault(state.users, [])];

        let orderMultiplier = state.usersSort.order == "ascending" ? 1 : -1;

        users.sort((a: User, b: User) => {
            let aVal = a[state.usersSort.by] || false;
            let bVal = b[state.usersSort.by] || false;

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
    static selectedUser(state: FabxStateModel, router: RouterStateModel): User | null {
        let id = router.state?.root.firstChild?.params['id'];

        if (state.users.tag == "FINISHED" && id) {
            return state.users.value.find(user => {
                return user.id == id;
            }) || null;
        } else {
            return null;
        }
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
                    console.error("error while getting all users: {}", err);
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
}
