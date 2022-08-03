import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthService } from "../services/auth.service";
import { Router } from "@angular/router";

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
        private loginService: AuthService,
        private router: Router
    ) { }

    onSubmit() {
        let username = this.form.get('username')!.value;
        let password = this.form.get('password')!.value;

        this.loginService.doLogin(username, password)
            .subscribe(val => {
                console.log("successful login: %o", val);
                this.error = "";

                this.router.navigateByUrl(`/user`);
            }, err => {
                console.log("error during login: %o", err);
                this.error = `Error: ${err.status} ${err.statusText}`
            });
    }
}
