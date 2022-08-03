import { Injectable } from "@angular/core";
import { CanActivate, Router, UrlTree } from "@angular/router";
import { Store } from "@ngxs/store";
import { FabxState } from "./fabx-state";
import { Observable } from "rxjs";

@Injectable()
export class AuthGuard implements CanActivate {
    private readonly loginUrl: UrlTree

    constructor(private store: Store, private router: Router) {
        this.loginUrl = router.parseUrl("/login");
    }

    canActivate(): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        return this.store.selectSnapshot(FabxState.isAuthenticated) || this.loginUrl;
    }
}
