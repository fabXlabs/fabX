import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { HttpErrorResponse } from "@angular/common/http";
import { Select, Store } from "@ngxs/store";
import { Auth } from "../state/auth.actions";
import { Navigate } from "@ngxs/router-plugin";
import { ErrorService } from "../services/error.service";
import { Observable, Subscription } from "rxjs";
import { FabxState } from "../state/fabx-state";

@Component({
    selector: 'fabx-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {

    error = "";

    showingPassword = false;

    form = new FormGroup({
        username: new FormControl('', Validators.required),
        password: new FormControl('')
    });

    @Select(FabxState.lastAuthenticatedUsername) lastAuthenticatedUsername$!: Observable<string | null>;
    private lastAuthenticatedUsernameSubscription: Subscription | null = null;

    constructor(
        private store: Store,
        private errorHandler: ErrorService
    ) { }


    ngOnInit(): void {
        this.lastAuthenticatedUsernameSubscription = this.lastAuthenticatedUsername$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        username: value
                    });
                }
            }
        });
    }

    onSubmit() {
        let username = this.form.get('username')!.value!;
        let password = this.form.get('password')!.value!;

        if (password) {
            this.login(new Auth.Login({ username: username, password: password }));
        } else {
            this.login(new Auth.LoginWebauthn(username));
        }
    }

    private login(action: any) {
        this.store.dispatch(action)
            .subscribe({
                next: _ => {
                    this.store.dispatch(new Navigate(['user']));
                },
                error: (err: HttpErrorResponse) => {
                    this.error = this.errorHandler.format(err);
                }
            });
    }

    ngOnDestroy(): void {
        this.lastAuthenticatedUsernameSubscription?.unsubscribe();
    }
}
