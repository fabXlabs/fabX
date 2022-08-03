import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { catchError, Observable, throwError } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private baseUrl = environment.baseUrl;

    constructor(private http: HttpClient) {}

    doLogin(username: string, password: string): Observable<Object> {
        localStorage.setItem("username", username);
        localStorage.setItem("password", password);

        return this.checkLogin();
    }

    checkLogin(): Observable<Object> {
        // TODO only get to-be-logged-in user
        return this.http.get(`${this.baseUrl}/user`, this.getOptions()).pipe(
            catchError((err: HttpErrorResponse, _) => {
                console.error("checkLogin error: %o", err)
                return throwError(() => err)
            })
        );
    }

    getOptions() {
        let username = localStorage.getItem("username");
        let password = localStorage.getItem("password");

        return {
            headers: new HttpHeaders({
                "Authorization": "Basic " + btoa(`${username}:${password}`)
            })
        };
    }

    doLogout(): void {
        localStorage.clear();
    }
}
