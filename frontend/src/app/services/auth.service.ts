import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { catchError, Observable, throwError } from "rxjs";
import { environment } from "../../environments/environment";
import { Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private baseUrl = environment.baseUrl;

    constructor(private http: HttpClient, private store: Store) {}

    login(username: string, password: string): Observable<Object> {
        // TODO only get to-be-logged-in user
        return this.http.get(`${this.baseUrl}/user`, {
            headers: new HttpHeaders({
                "Authorization": "Basic " + btoa(`${username}:${password}`)
            })
        }).pipe(
            catchError((err: HttpErrorResponse, _) => {
                console.error("checkLogin error: %o", err)
                return throwError(() => err)
            })
        );
    }

    getOptions() {
        let auth = this.store.selectSnapshot(FabxState.auth);

        if (auth) {
            return {
                headers: new HttpHeaders({
                    "Authorization": "Basic " + btoa(`${auth.username}:${auth.password}`)
                })
            };
        } else {
            return {};
        }
    }

    doLogout(): void {
        localStorage.clear();
    }
}
