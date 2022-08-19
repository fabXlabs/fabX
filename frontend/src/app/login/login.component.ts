import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { HttpErrorResponse } from "@angular/common/http";
import { Store } from "@ngxs/store";
import { Auth } from "../state/auth.actions";
import { Navigate } from "@ngxs/router-plugin";
import { ErrorService } from "../services/error.service";
import { AuthService } from "../services/auth.service";

@Component({
    selector: 'fabx-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {

    error = "";

    form = new FormGroup({
        username: new FormControl('', Validators.required),
        password: new FormControl('')
    });

    constructor(
        private store: Store,
        private errorHandler: ErrorService,
        private authService: AuthService
    ) { }

    onSubmit() {
        let username = this.form.get('username')!.value;
        let password = this.form.get('password')!.value;

        this.store.dispatch(new Auth.Login({ username: username, password: password }))
            .subscribe({
                next: _ => {
                    this.store.dispatch(new Navigate(['user']));
                },
                error: (err: HttpErrorResponse) => {
                    this.error = this.errorHandler.format(err);
                }
            });
    }

    loginWebauthn() {
        let username = this.form.get('username')!.value;
        this.authService.loginWebauthn(username);
    }
}
