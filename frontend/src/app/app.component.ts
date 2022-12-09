import { Component, Inject, OnInit } from '@angular/core';
import { DOCUMENT } from "@angular/common";
import { Actions, ofActionCompleted, ofActionDispatched, Store } from "@ngxs/store";
import { Navigate } from "@ngxs/router-plugin";
import { Auth } from "./state/auth.actions";
import { Devices } from "./state/device.actions";
import { FabxState } from "./state/fabx-state";
import { Qualifications } from "./state/qualification.action";
import { Users } from "./state/user.actions";
import { Tools } from "./state/tool.actions";


declare const process: { env: { FABX_VERSION: string; }; };

@Component({
    selector: 'fabx-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    title = 'fabX';

    fabxVersion = process.env.FABX_VERSION;

    constructor(
        private store: Store,
        private actions: Actions,
        @Inject(DOCUMENT) private document: Document
    ) {}

    ngOnInit(): void {
        this.loadAll();

        this.actions.pipe(ofActionCompleted(Auth.Login, Auth.LoginWebauthn)).subscribe(() => {
            this.loadAll();
        });

        this.actions.pipe(ofActionDispatched(Auth.Logout)).subscribe(() => {
            this.store.dispatch(new Navigate(['/login']));
        });

        // TODO refactor ThemeService
        let themeLink = this.document.getElementById('app-theme') as HTMLLinkElement;

        const darkModeOn = window.matchMedia("(prefers-color-scheme: dark)").matches;

        if (darkModeOn) {
            themeLink.href = 'theme-dark.css';
        }

        window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", e => {
            const darkModeOn = e.matches;

            if (darkModeOn) {
                themeLink.href = 'theme-dark.css';
            } else {
                themeLink.href = 'theme-light.css';
            }
        });
    }

    loadAll() {
        if (this.store.selectSnapshot(FabxState.isAuthenticated)) {
            this.store.dispatch(Users.GetMe).subscribe({
                next: _ => {
                    const loggedInUser = this.store.selectSnapshot(FabxState.loggedInUser)
                    if (loggedInUser) {
                        if (loggedInUser.isAdmin) {
                            this.store.dispatch(Devices.GetAll);
                            this.store.dispatch(Users.GetAll);
                        }
                        this.store.dispatch(Tools.GetAll);
                        this.store.dispatch(Qualifications.GetAll);
                    }
                }
            });
        }
    }
}
