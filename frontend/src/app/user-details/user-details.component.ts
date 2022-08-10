import { Component, OnDestroy } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { Qualification } from "../models/qualification.model";
import { MenuItem } from "primeng/api";
import { Users } from "../state/user.actions";

@Component({
    selector: 'fabx-user-details',
    templateUrl: './user-details.component.html',
    styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent implements OnDestroy {

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    @Select(FabxState.availableMemberQualificationsForSelectedUser) availableMemberQualifications$!: Observable<Qualification[]>;
    @Select(FabxState.availableInstructorQualificationsForSelectedUser) availableInstructorQualifications$!: Observable<Qualification[]>;
    private availableMemberQualificationsSubscription: Subscription;
    private availableInstructorQualificationsSubscription: Subscription;

    memberQualificationItems: MenuItem[] = [];
    instructorQualificationItems: MenuItem[] = [];

    constructor(private store: Store) {
        this.availableMemberQualificationsSubscription = this.availableMemberQualifications$.subscribe({
            next: value => {
                this.memberQualificationItems = value.map(qualification => {
                    return {
                        label: qualification.name,
                        command: _ => this.addMemberQualification(qualification.id)
                    };
                });
            }
        });

        this.availableInstructorQualificationsSubscription = this.availableInstructorQualifications$.subscribe({
            next: value => {
                this.instructorQualificationItems = value.map(qualification => {
                    return {
                        label: qualification.name,
                        command: _ => this.addInstructorQualification(qualification.id)
                    };
                });
            }
        });
    }

    addMemberQualification(qualificationId: string) {
        this.store.dispatch(new Users.AddMemberQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        ));
    }

    removeMemberQualification(qualificationId: string) {
        this.store.dispatch(new Users.RemoveMemberQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        ));
    }

    addInstructorQualification(qualificationId: string) {
        this.store.dispatch(new Users.AddInstructorQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        ));
    }

    removeInstructorQualification(qualificationId: string) {
        this.store.dispatch(new Users.RemoveInstructorQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        ));
    }

    ngOnDestroy(): void {
        this.availableMemberQualificationsSubscription.unsubscribe();
    }
}
