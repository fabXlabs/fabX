import { Component, OnDestroy, OnInit } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedUser, User } from "../models/user.model";
import { Qualification } from "../models/qualification.model";
import { ConfirmationService, MenuItem, MessageService } from "primeng/api";
import { Users } from "../state/user.actions";
import { ErrorService } from "../services/error.service";
import { UserService } from "../services/user.service";

@Component({
    selector: 'fabx-user-details',
    templateUrl: './user-details.component.html',
    styleUrls: ['./user-details.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class UserDetailsComponent implements OnInit, OnDestroy {

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    @Select(FabxState.loggedInUser) loggedInUser$!: Observable<User>;
    @Select(FabxState.availableMemberQualificationsForSelectedUser) availableMemberQualifications$!: Observable<Qualification[]>;
    @Select(FabxState.availableInstructorQualificationsForSelectedUser) availableInstructorQualifications$!: Observable<Qualification[]>;
    private availableMemberQualificationsSubscription: Subscription | null = null;
    private availableInstructorQualificationsSubscription: Subscription | null = null;

    memberQualificationItems: MenuItem[] = [];
    instructorQualificationItems: MenuItem[] = [];

    constructor(
        private store: Store,
        private confirmationService: ConfirmationService,
        private messageService: MessageService,
        private errorService: ErrorService,
        public userService: UserService
    ) {}

    ngOnInit() {
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
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Adding Member Qualification',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removeMemberQualification(qualificationId: string) {
        this.store.dispatch(new Users.RemoveMemberQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Member Qualification',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    addInstructorQualification(qualificationId: string) {
        this.store.dispatch(new Users.AddInstructorQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Adding Instructor Qualification',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removeInstructorQualification(qualificationId: string) {
        this.store.dispatch(new Users.RemoveInstructorQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Instructor Qualification',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removeUsernamePasswordIdentity(username: string) {
        this.store.dispatch(new Users.RemoveUsernamePasswordIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            username
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Username/Password Identity',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removeWebauthnIdentity(credentialId: number[]) {
        this.store.dispatch(new Users.RemoveWebauthnIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            credentialId
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Webauthn Identity',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removeCardIdentity(cardId: string) {
        this.store.dispatch(new Users.RemoveCardIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            cardId
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Card Identity',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removePhoneNrIdentity(phoneNr: string) {
        this.store.dispatch(new Users.RemovePhoneNrIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            phoneNr
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Phone Nr. Identity',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    removePinIdentity() {
        this.store.dispatch(new Users.RemovePinIdentity(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Removing Pin Identity',
                    detail: message,
                    sticky: true
                });
            }
        });
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
                )).subscribe({
                    error: err => {
                        const message = this.errorService.format(err);
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error Changing Admin Privilege',
                            detail: message,
                            sticky: true
                        });
                    }
                });
            }
        });
    }

    delete() {
        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        let message = `Are you sure you want to delete user ${currentUser.firstName} ${currentUser.lastName}?`

        this.confirmationService.confirm({
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            acceptButtonStyleClass: 'p-button-danger',
            acceptIcon: 'pi pi-trash',
            rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
            message: message,
            accept: () => {
                this.store.dispatch(new Users.Delete(
                    currentUser.id
                )).subscribe({
                    error: err => {
                        const message = this.errorService.format(err);
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error Deleting User',
                            detail: message,
                            sticky: true
                        });
                    }
                });
            }
        });
    }

    ngOnDestroy(): void {
        this.availableMemberQualificationsSubscription?.unsubscribe();
        this.availableInstructorQualificationsSubscription?.unsubscribe();
    }
}
