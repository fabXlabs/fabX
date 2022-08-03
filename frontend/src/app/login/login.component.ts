import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { Store } from "@ngxs/store";
import { Auth } from "../state/auth.actions";

@Component({
    selector: 'fabx-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {

    error = "";

    form = new FormGroup({
        username: new FormControl('', Validators.required),
        password: new FormControl('', Validators.required)
    });

    constructor(
        private store: Store,
        private router: Router
    ) { }

    onSubmit() {
        let username = this.form.get('username')!.value;
        let password = this.form.get('password')!.value;

        this.store.dispatch(new Auth.Login({ username: username, password: password }))
            .subscribe({
                next: _ => {
                    this.router.navigateByUrl(`/user`);
                },
                error: (err: HttpErrorResponse) => {
                    console.log("error during login: %o", err);
                    this.error = `Error: ${err.statusText} (${err.status})`
                    if (err.error) {
                        this.error += ` ${JSON.stringify(err.error)}`
                    }
                }
            });
    }
}
