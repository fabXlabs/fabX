import { Injectable } from "@angular/core";
import { Action, Selector, State, StateContext } from "@ngxs/store";
import { UserService } from "../services/user.service";
import { User } from "../models/user.model";
import { Users } from "./user.actions";
import { tap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { getFinishedValueOrDefault, LoadingState, LoadingStateTag } from "./loading-state.model";

export interface FabxStateModel {
    users: LoadingState<User[]>
}

@State<FabxStateModel>({
    name: 'fabx',
    defaults: {
        users: { tag: "LOADING" },
    }
})
@Injectable()
export class FabxState {
    constructor(private userService: UserService) {}

    @Selector()
    static users(state: FabxStateModel): User[] {
        return getFinishedValueOrDefault(state.users, []);
    }

    @Selector()
    static usersLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.users.tag;
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
}
