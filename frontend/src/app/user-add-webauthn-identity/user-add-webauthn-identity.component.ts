import { Component } from '@angular/core';
import { UserService } from "../services/user.service";
import { Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";

@Component({
    selector: 'fabx-user-add-webauthn-identity',
    templateUrl: './user-add-webauthn-identity.component.html',
    styleUrls: ['./user-add-webauthn-identity.component.scss']
})
export class UserAddWebauthnIdentityComponent {

    constructor(private store: Store, private userService: UserService) { }

    register() {
        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.userService.registerWebauthn(currentUser.id).subscribe({
            next: registrationDetails => {
                console.log("registrationDetails", registrationDetails);

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
                console.log("options", options);

                navigator.credentials.create(options)
                    .then((response) => {
                        console.log("response", response);

                        if (response) {
                            const pkc = response as PublicKeyCredential;
                            const r = pkc.response as AuthenticatorAttestationResponse;

                            const attestationArray = new Int8Array(r.attestationObject);
                            const clientDataArray = new Int8Array(pkc.response.clientDataJSON);

                            this.userService.responseWebauthn(
                                currentUser.id,
                                Array.from(attestationArray),
                                Array.from(clientDataArray)
                            ).subscribe({
                                next: value => {
                                    console.log("value", value);
                                }
                            });
                        }
                    });
            }
        })
    }
}
