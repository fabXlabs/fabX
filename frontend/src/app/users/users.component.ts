import { Component, OnInit } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { Observable } from "rxjs";
import { User } from "../models/user.model";
import { Users } from "../state/user.actions";
import { FabxState } from "../state/fabx-state";
import { LoadingStateTag } from "../state/loading-state.model";
import { SortEvent } from "primeng/api";

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

    customSort(event: SortEvent) {
        const order = event.order == 1 ? "ascending" : "descending";

        let fieldAccessor: keyof User = "id";
        switch (event.field) {
            case "id":
                fieldAccessor = "id";
                break;
            case "firstName":
                fieldAccessor = "firstName";
                break;
            case "lastName":
                fieldAccessor = "lastName";
                break;
            case "wikiName":
                fieldAccessor = "wikiName";
                break;
            case "isAdmin":
                fieldAccessor = "isAdmin";
                break;
        }

        this.store.dispatch(new Users.SetSort({
            by: fieldAccessor,
            order: order
        }));
    }
}
