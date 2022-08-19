import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { ErrorService } from "../services/error.service";
import { ChangeableValue } from "../models/changeable-value";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-user-change-lock-state',
    templateUrl: './user-change-lock-state.component.html',
    styleUrls: ['./user-change-lock-state.component.scss']
})
export class UserChangeLockStateComponent implements OnInit, OnDestroy {

    error = "";

    form = new FormGroup({
        locked: new FormControl(false),
        notes: new FormControl<string | null>(null)
    });

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    private selectedUserSubscription: Subscription | null = null;

    constructor(private store: Store, private errorHandler: ErrorService) {}

    ngOnInit() {
        this.selectedUserSubscription = this.user$.subscribe({
            next: value => {
                if (value) {
                    console.log("next", value);
                    this.form.patchValue({
                        locked: value.locked,
                        notes: value.notes
                    });
                }
            }
        });
    }

    get lockedForm() {
        return this.form.controls['locked'] as FormControl;
    }

    onSubmit() {
        const locked = this.form.get('locked')!.value;
        let notes = this.form.get('notes')!.value;
        if (!notes) {
            notes = null
        }

        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;
        console.log("currentUser", currentUser);

        let lockedChange: ChangeableValue<boolean> | null = null;
        if (locked != currentUser.locked) {
            lockedChange = {
                newValue: locked!
            }
        }

        let notesChange: ChangeableValue<string | null> | null = null;
        if (notes != currentUser.lastName) {
            notesChange = {
                newValue: notes!
            }
        }

        this.store.dispatch(new Users.ChangeLockState(
            currentUser.id,
            {
                locked: lockedChange,
                notes: notesChange
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    ngOnDestroy() {
        if (this.selectedUserSubscription) {
            this.selectedUserSubscription.unsubscribe();
        }
    }
}
