import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { Observable } from "rxjs";
import { User, UserVM } from "../models/user.model";
import { Users } from "../state/user.actions";
import { FabxState } from "../state/fabx-state";
import { LoadingStateTag } from "../state/loading-state.model";
import { LazyLoadEvent } from "primeng/api";

@Component({
    selector: 'fabx-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss']
})
export class UsersComponent {

    @Select(FabxState.usersLoadingState) loading$!: Observable<LoadingStateTag>;
    @Select(FabxState.users) users$!: Observable<UserVM[]>;

    constructor(private store: Store) {}

    lazyLoad(event: LazyLoadEvent) {
        const order = event.sortOrder == 1 ? "ascending" : "descending";

        if (event.sortField) {
            let fieldAccessor: keyof User = "id";
            switch (event.sortField) {
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
}
