import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import {
    CardIdentityAdditionDetails,
    IsAdminDetails,
    PhoneNrIdentityAdditionDetails,
    QualificationAdditionDetails,
    User,
    UserCreationDetails,
    UserDetails,
    UserLockDetails,
    UsernamePasswordIdentityAdditionDetails
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
        return this.http.post<string>(
            `${this.baseUrl}/user`,
            details,
            this.authService.getOptions()
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
        };

        return this.http.put(
            `${this.baseUrl}/user/${userId}/is-admin`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public addUsernamePasswordIdentity(userId: string, username: string, password: string): Observable<string> {
        const details: UsernamePasswordIdentityAdditionDetails = {
            username: username,
            password: password
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/identity/username-password`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removeUsernamePasswordIdentity(userId: string, username: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/identity/username-password/${username}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public addCardIdentity(userId: string, cardId: string, cardSecret: string): Observable<string> {
        const details: CardIdentityAdditionDetails = {
            cardId: cardId,
            cardSecret: cardSecret
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/identity/card`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removeCardIdentity(userId: string, cardId: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/identity/card/${cardId}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public addPhoneNrIdentity(userId: string, phoneNr: string): Observable<string> {
        const details: PhoneNrIdentityAdditionDetails = {
            phoneNr: phoneNr
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/identity/phone`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removePhoneNrIdentity(userId: string, phoneNr: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/identity/phone/${phoneNr}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public deleteUser(id: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${id}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        )
    }
}

