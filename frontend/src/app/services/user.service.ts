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
        private authService: AuthService
    ) { }

    public getAllUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.baseUrl}/user`, this.authService.getOptions()).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<User> {
        return this.http.get<User>(`${this.baseUrl}/user/${id}`, this.authService.getOptions());
    }

    public addUser(details: UserCreationDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/user`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }
}

