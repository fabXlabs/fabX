import { Component, OnDestroy } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { ChangeableValue } from "../models/changeable-value";

@Component({
    selector: 'fabx-user-change-personal-info',
    templateUrl: './user-change-personal-info.component.html',
    styleUrls: ['./user-change-personal-info.component.scss']
})
export class UserChangePersonalInfoComponent implements OnDestroy {

    error = "";

    form = new FormGroup({
        firstName: new FormControl('', Validators.required),
        lastName: new FormControl('', Validators.required),
        wikiName: new FormControl('', Validators.required),
    });

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    private selectedUserSubscription: Subscription;

    constructor(private store: Store, private errorHandler: ErrorService) {
        this.selectedUserSubscription = this.user$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        firstName: value.firstName,
                        lastName: value.lastName,
                        wikiName: value.wikiName,
                    });
                }
            }
        });
    }

    onSubmit() {
        const firstName = this.form.get('firstName')!.value;
        const lastName = this.form.get('lastName')!.value;
        const wikiName = this.form.get('wikiName')!.value;

        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;
        console.log("currentUser", currentUser);

        let firstNameChange: ChangeableValue<string> | null = null;
        if (firstName != currentUser.firstName) {
            firstNameChange = {
                newValue: firstName
            }
        }

        let lastNameChange: ChangeableValue<string> | null = null;
        if (lastName != currentUser.lastName) {
            lastNameChange = {
                newValue: lastName
            }
        }

        let wikiNameChange: ChangeableValue<string> | null = null;
        if (wikiName != currentUser.wikiName) {
            wikiNameChange = {
                newValue: wikiName
            }
        }

        this.store.dispatch(new Users.ChangePersonalInformation(
            currentUser.id,
            {
                firstName: firstNameChange,
                lastName: lastNameChange,
                wikiName: wikiNameChange
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    ngOnDestroy() {
        this.selectedUserSubscription.unsubscribe();
    }
}
