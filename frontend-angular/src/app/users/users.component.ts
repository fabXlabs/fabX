import { Component, Input } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { Observable } from "rxjs";
import { AugmentedUser, User } from "../models/user.model";
import { Users } from "../state/user.actions";
import { FabxState } from "../state/fabx-state";
import { LoadingStateTag } from "../state/loading-state.model";
import { ErrorService } from "../services/error.service";
import { TableLazyLoadEvent } from "primeng/table";

@Component({
    selector: 'fabx-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss']
})
export class UsersComponent {

    @Select(FabxState.usersLoadingState) loading$!: Observable<LoadingStateTag>;
    @Select(FabxState.users) users$!: Observable<AugmentedUser[]>;
    @Select(FabxState.loggedInUser) loggedInUser$!: Observable<User>;

    @Input() filterText: string = ""

    constructor(
        private store: Store,
        private errorService: ErrorService
    ) {
        this.filterText = this.store.selectSnapshot(FabxState.userFilter) || "";
    }

    lazyLoad(event: TableLazyLoadEvent) {
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

    onFilterInput(_: Event) {
        this.store.dispatch(new Users.SetFilter(this.filterText))
            .subscribe({
                error: err => {
                    const message = this.errorService.format(err);
                    console.error(message);
                }
            });
    }

    onFilterClear() {
        this.store.dispatch(new Users.SetFilter(null))
            .subscribe({
                next: _ => {
                    this.filterText = "";
                },
                error: err => {
                    const message = this.errorService.format(err);
                    console.error(message);
                }
            });
    }
}
