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
    selector: 'fabx-user-add-phone-nr-identity',
    templateUrl: './user-add-phone-nr-identity.component.html',
    styleUrls: ['./user-add-phone-nr-identity.component.scss']
})
export class UserAddPhoneNrIdentityComponent {

    error = "";

    form = new FormGroup({
        phoneNr: new FormControl("")
    });

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const phoneNr = this.form.get('phoneNr')!.value!;

        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.store.dispatch(new Users.AddPhoneNrIdentity(
            currentUser.id,
            {
                phoneNr: phoneNr
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
