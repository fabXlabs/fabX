import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Users } from "../state/user.actions";
import { Observable } from "rxjs";
import { AugmentedUser } from "../models/user.model";

@Component({
    selector: 'fabx-user-add-webauthn-identity',
    templateUrl: './user-add-webauthn-identity.component.html',
    styleUrls: ['./user-add-webauthn-identity.component.scss']
})
export class UserAddWebauthnIdentityComponent {

    error = "";

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;

    constructor(private store: Store) { }

    register() {
        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.store.dispatch(new Users.AddWebauthnIdentity(currentUser.id))
            .subscribe({
                error: (err: Error) => {
                    this.error = err.message;
                }
            });
    }
}
