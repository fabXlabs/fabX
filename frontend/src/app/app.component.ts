import { Component, Inject, OnInit } from '@angular/core';
import { DOCUMENT } from "@angular/common";
import { Actions, ofActionCompleted, ofActionDispatched, Store } from "@ngxs/store";
import { Users } from "./state/user.actions";
import { FabxState } from "./state/fabx-state";
import { Auth } from "./state/auth.actions";
import { Navigate } from "@ngxs/router-plugin";
import { Qualifications } from "./state/qualification.action";

@Component({
    selector: 'fabx-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    title = 'fabX';

    constructor(
        private store: Store,
        private actions: Actions,
        @Inject(DOCUMENT) private document: Document
    ) {}

    ngOnInit(): void {
        this.loadAll();

        this.actions.pipe(ofActionCompleted(Auth.Login)).subscribe(() => {
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
            this.store.dispatch(Users.GetAll);
            this.store.dispatch(Qualifications.GetAll);
        }
    }
}
