import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import { User, UserCreationDetails } from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    private baseUrl = environment.baseUrl;

    constructor(
        private http: HttpClient,
        private loginService: AuthService
    ) { }

    public getAllUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.baseUrl}/user`, this.loginService.getOptions()).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<User> {
        return this.http.get<User>(`${this.baseUrl}/user/${id}`, this.loginService.getOptions());
    }

    public addUser(details: UserCreationDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/user`,
            details,
            {
                ...this.loginService.getOptions(),
                responseType: 'text'
            }
        );
    }
}

