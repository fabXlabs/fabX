import { Component } from '@angular/core';
import { FormControl, FormGroup } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { ErrorService } from "../services/error.service";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'fabx-user-add-username-password-identity',
    templateUrl: './user-add-username-password-identity.component.html',
    styleUrls: ['./user-add-username-password-identity.component.scss']
})
export class UserAddUsernamePasswordIdentityComponent {

    error = "";

    form = new FormGroup({
        username: new FormControl(""),
        password: new FormControl("")
    });

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;

    constructor(private store: Store, private errorHandler: ErrorService, private authService: AuthService) { }

    onSubmit() {
        const username = this.form.get('username')!.value!;
        const password = this.form.get('password')!.value!;

        if (!this.authService.validatePassword(password)) {
            this.error = "Password is not long enough (8 characters min.) or contains forbidden characters (characters 0x21 through 0x7e of the ASCII set are allowed)."
            return;
        }

        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.store.dispatch(new Users.AddUsernamePasswordIdentity(
            currentUser.id,
            {
                username: username,
                password: password
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
