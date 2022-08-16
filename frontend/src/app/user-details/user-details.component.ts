import { Component, OnDestroy } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedUser, User } from "../models/user.model";
import { Qualification } from "../models/qualification.model";
import { ConfirmationService, MenuItem } from "primeng/api";
import { Users } from "../state/user.actions";

@Component({
    selector: 'fabx-user-details',
    templateUrl: './user-details.component.html',
    styleUrls: ['./user-details.component.scss'],
    providers: [ConfirmationService]
})
export class UserDetailsComponent implements OnDestroy {

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    @Select(FabxState.loggedInUser) loggedInUser$!: Observable<User>;
    @Select(FabxState.availableMemberQualificationsForSelectedUser) availableMemberQualifications$!: Observable<Qualification[]>;
    @Select(FabxState.availableInstructorQualificationsForSelectedUser) availableInstructorQualifications$!: Observable<Qualification[]>;
    private availableMemberQualificationsSubscription: Subscription;
    private availableInstructorQualificationsSubscription: Subscription;

    memberQualificationItems: MenuItem[] = [];
    instructorQualificationItems: MenuItem[] = [];

    constructor(private store: Store, private confirmationService: ConfirmationService) {
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

    removeUsernamePasswordIdentity(username: string) {
        this.store.dispatch(new Users.RemoveUsernamePasswordIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            username
        ));
    }

    removeCardIdentity(cardId: string) {
        this.store.dispatch(new Users.RemoveCardIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            cardId
        ));
    }

    removePhoneNrIdentity(phoneNr: string) {
        this.store.dispatch(new Users.RemovePhoneNrIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            phoneNr
        ));
    }

    toggleAdmin(_: any) {
        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        let message = `Are you sure you want to grant admin rights to ${currentUser.firstName} ${currentUser.lastName}?`
        if (currentUser.isAdmin) {
            message = `Are you sure you want to remove admin rights from ${currentUser.firstName} ${currentUser.lastName}?`
        }

        this.confirmationService.confirm({
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            message: message,
            accept: () => {
                this.store.dispatch(new Users.ChangeIsAdmin(
                    currentUser.id,
                    !currentUser.isAdmin
                ));
            }
        });
    }

    ngOnDestroy(): void {
        this.availableMemberQualificationsSubscription.unsubscribe();
        this.availableInstructorQualificationsSubscription.unsubscribe();
    }
}
