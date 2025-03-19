import { Component } from '@angular/core';
import { FormControl, FormGroup } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { ErrorService } from "../services/error.service";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-user-add-pin-identity',
    templateUrl: './user-add-pin-identity.component.html',
    styleUrls: ['./user-add-pin-identity.component.scss'],
})
export class UserAddPinIdentityComponent {

    error = "";

    form = new FormGroup({
        pin: new FormControl("")
    });

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const pin = this.form.get('pin')!.value!;

        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.store.dispatch(new Users.AddPinIdentity(
            currentUser.id,
            {
                pin: pin
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
