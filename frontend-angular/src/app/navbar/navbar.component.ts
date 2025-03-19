import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { User } from "../models/user.model";
import { Auth } from "../state/auth.actions";

@Component({
    selector: 'fabx-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {

    @Select(FabxState.loggedInUser) loggedInUser$!: Observable<User>;

    constructor(private store: Store) { }

    onLogout() {
        this.store.dispatch(Auth.Logout)
    }
}
