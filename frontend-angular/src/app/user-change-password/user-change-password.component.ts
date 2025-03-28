import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { ErrorService } from "../services/error.service";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'fabx-user-change-password',
    templateUrl: './user-change-password.component.html',
    styleUrls: ['./user-change-password.component.scss']
})
export class UserChangePasswordComponent {

    error = "";

    form = new FormGroup({
        password: new FormControl("", Validators.required),
        password2: new FormControl("", Validators.required)
    });

    @Select(FabxState.loggedInUser) user$!: Observable<AugmentedUser>;

    constructor(private store: Store, private errorHandler: ErrorService, private authService: AuthService) { }

    onSubmit() {
        const password = this.form.get('password')!.value!;
        const password2 = this.form.get('password2')!.value!;

        if (!this.authService.validatePassword(password)) {
            this.error = "Password is not long enough (8 characters min.) or contains forbidden characters (characters 0x21 through 0x7e of the ASCII set are allowed)."
            return;
        }

        if (password != password2) {
            this.error = "Password not identical.";
            return;
        }

        const currentUser = this.store.selectSnapshot(FabxState.loggedInUser)!;

        this.store.dispatch(new Users.ChangePassword(
            currentUser.id,
            { password: password }
        )).subscribe({
            next: val => {
                console.log("DONE CHANGE PASSWORD", val);
            },
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
