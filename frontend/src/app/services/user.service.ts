import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import {
    IsAdminDetails,
    QualificationAdditionDetails,
    User,
    UserCreationDetails,
    UserDetails,
    UserLockDetails
} from '../models/user.model';

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

    public changePersonalInformation(userId: string, personalInformation: UserDetails): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/user/${userId}`,
            personalInformation,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public changeLockState(userId: string, lockDetails: UserLockDetails): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/user/${userId}/lock`,
            lockDetails,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        )
    }

    public addMemberQualification(userId: string, qualificationId: string): Observable<string> {
        const details: QualificationAdditionDetails = {
            qualificationId: qualificationId
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/member-qualification`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removeMemberQualification(userId: string, qualificationId: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/member-qualification/${qualificationId}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public addInstructorQualification(userId: string, qualificationId: string): Observable<string> {
        const details: QualificationAdditionDetails = {
            qualificationId: qualificationId
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/instructor-qualification`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removeInstructorQualification(userId: string, qualificationId: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/instructor-qualification/${qualificationId}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public changeIsAdmin(userId: string, isAdmin: boolean): Observable<string> {
        const details: IsAdminDetails = {
            isAdmin: isAdmin
        }

        return this.http.put(
            `${this.baseUrl}/user/${userId}/is-admin`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        )
    }
}

