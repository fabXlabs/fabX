import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { mergeMap, Observable, retry } from "rxjs";
import {
    CardIdentityAdditionDetails,
    ChangePasswordDetails,
    IsAdminDetails,
    PhoneNrIdentityAdditionDetails,
    PinIdentityAdditionDetails,
    QualificationAdditionDetails,
    User,
    UserCreationDetails,
    UserDetails,
    UserLockDetails,
    UsernamePasswordIdentityAdditionDetails,
    WebauthnIdentityAdditionDetails,
    WebauthnRegistrationDetails
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

    public getMe(): Observable<User> {
        return this.http.get<User>(`${this.baseUrl}/user/me`, this.authService.getOptions());
    }

    public getIdByWikiName(wikiName: string): Observable<string> {
        return this.http.get(
            `${this.baseUrl}/user/id-by-wiki-name?wikiName=${wikiName}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
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

    public changePassword(userId: string, password: string): Observable<string> {
        const details: ChangePasswordDetails = { password: password }

        return this.http.post(
            `${this.baseUrl}/user/${userId}/identity/username-password/change-password`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        )
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

    public addWebauthnIdentity(userId: string): Observable<string> {
        return this.registerWebauthn(userId).pipe(
            mergeMap((registrationDetails) => {
                const challengeArray = new Int8Array(registrationDetails.challenge);
                const userIdArray = new Int8Array(registrationDetails.userId);

                let options: CredentialCreationOptions = {
                    publicKey: {
                        attestation: registrationDetails.attestation,
                        challenge: challengeArray.buffer,
                        rp: {
                            id: registrationDetails.rpId,
                            name: registrationDetails.rpName
                        },
                        user: {
                            id: userIdArray.buffer,
                            name: registrationDetails.userName,
                            displayName: registrationDetails.userDisplayName
                        },
                        pubKeyCredParams: registrationDetails.pubKeyCredParams
                    }
                };

                return navigator.credentials.create(options);
            }),
            mergeMap((credential) => {
                if (credential) {
                    const pkc = credential as PublicKeyCredential;
                    const r = pkc.response as AuthenticatorAttestationResponse;

                    const attestationArray = new Int8Array(r.attestationObject);
                    const clientDataArray = new Int8Array(pkc.response.clientDataJSON);

                    return this.responseWebauthn(
                        userId,
                        Array.from(attestationArray),
                        Array.from(clientDataArray)
                    );
                } else {
                    throw Error("Not able to get credential");
                }
            })
        );
    }

    private registerWebauthn(userId: string): Observable<WebauthnRegistrationDetails> {
        return this.http.post<WebauthnRegistrationDetails>(
            `${this.baseUrl}/user/${userId}/identity/webauthn/register`,
            {},
            this.authService.getOptions()
        );
    }

    private responseWebauthn(
        userId: string,
        attestationObject: number[],
        clientDataJSON: number[]
    ): Observable<string> {
        const details: WebauthnIdentityAdditionDetails = {
            attestationObject: attestationObject,
            clientDataJSON: clientDataJSON
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/identity/webauthn/response`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removeWebauthnIdentity(userId: string, credentialId: number[]): Observable<string> {
        const credentialIdHex = this.toHexString(credentialId)
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/identity/webauthn/${credentialIdHex}`,

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

    public addPinIdentity(userId: string, pin: string): Observable<string> {
        const details: PinIdentityAdditionDetails = {
            pin: pin
        };

        return this.http.post(
            `${this.baseUrl}/user/${userId}/identity/pin`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public removePinIdentity(userId: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/${userId}/identity/pin`,
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

    public getSoftDeletedUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.baseUrl}/user/soft-deleted`, this.authService.getOptions()).pipe(
            retry(3)
        );
    }

    public hardDelete(id: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/user/soft-deleted/${id}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        )
    }

    toHexString(arr: number[]): string {
        return Array.from(arr, function (byte) {
            return ('0' + (byte & 0xFF).toString(16)).slice(-2);
        }).join('')
    }
}

