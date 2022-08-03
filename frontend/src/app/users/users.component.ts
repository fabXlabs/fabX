import { Component, OnInit } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { Observable } from "rxjs";
import { User } from "../models/user.model";
import { Users } from "../state/user.actions";
import { FabxState } from "../state/fabx-state";
import { LoadingStateTag } from "../state/loading-state.model";

// TODO fix sorting (tries to sort list in place which is incompatible with Observable)
@Component({
    selector: 'fabx-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

    @Select(FabxState.usersLoadingState) loading!: Observable<LoadingStateTag>;
    @Select(FabxState.users) users$!: Observable<User[]>;

    constructor(private store: Store) {}

    ngOnInit(): void {
        this.store.dispatch(new Users.GetAll())
    }
}
