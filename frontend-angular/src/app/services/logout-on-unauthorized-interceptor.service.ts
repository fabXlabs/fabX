import { Injectable } from "@angular/core";
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpStatusCode
} from "@angular/common/http";
import { catchError, Observable, throwError } from "rxjs";
import { Store } from "@ngxs/store";
import { Auth } from "../state/auth.actions";
import { FabxState } from "../state/fabx-state";

@Injectable()
export class LogoutOnUnauthorizedInterceptorService implements HttpInterceptor {

    constructor(private store: Store) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(catchError((err: HttpErrorResponse) => {
            if (err.status == HttpStatusCode.Unauthorized && this.store.selectSnapshot(FabxState.isAuthenticated)) {
                return this.store.dispatch(Auth.Logout);
            } else {
                return throwError(() => err);
            }
        }));
    }
}
