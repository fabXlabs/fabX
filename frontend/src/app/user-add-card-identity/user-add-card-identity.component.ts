import { Component } from '@angular/core';
import { FormControl, FormGroup } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { ErrorService } from "../services/error.service";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-user-add-card-identity',
    templateUrl: './user-add-card-identity.component.html',
    styleUrls: ['./user-add-card-identity.component.scss']
})
export class UserAddCardIdentityComponent {

    error = "";

    form = new FormGroup({
        cardId: new FormControl(""),
        cardSecret: new FormControl("")
    });

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const cardId = this.form.get('cardId')!.value;
        const cardSecret = this.form.get('cardSecret')!.value;

        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.store.dispatch(new Users.AddCardIdentity(
            currentUser.id,
            {
                cardId: cardId,
                cardSecret: cardSecret
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
