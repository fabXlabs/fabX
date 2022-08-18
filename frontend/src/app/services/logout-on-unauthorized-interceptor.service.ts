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

@Injectable()
export class LogoutOnUnauthorizedInterceptorService implements HttpInterceptor {

    constructor(private store: Store) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(catchError((err: HttpErrorResponse) => {
            if (err.status == HttpStatusCode.Unauthorized) {
                return this.store.dispatch(Auth.Logout);
            } else {
                return throwError(() => err);
            }
        }));
    }
}
