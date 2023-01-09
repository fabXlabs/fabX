import { Component, OnInit } from '@angular/core';
import { UserService } from "../services/user.service";
import { Observable } from "rxjs";
import { User } from "../models/user.model";
import { ConfirmationService, MessageService } from "primeng/api";
import { ErrorService } from "../services/error.service";

@Component({
    selector: 'fabx-user-soft-deleted',
    templateUrl: './user-soft-deleted.component.html',
    styleUrls: ['./user-soft-deleted.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class UserSoftDeletedComponent implements OnInit {

    softDeletedUsers$!: Observable<User[]>;

    constructor(
        private userService: UserService,
        private confirmationService: ConfirmationService,
        private messageService: MessageService,
        private errorService: ErrorService,
    ) {}

    ngOnInit(): void {
        this.softDeletedUsers$ = this.userService.getSoftDeletedUsers();

    }

    hardDelete(user: User): void {

        let message = `Are you sure you want to hard delete user ${user.firstName} ${user.lastName}?`

        this.confirmationService.confirm({
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            acceptButtonStyleClass: 'p-button-danger',
            acceptIcon: 'pi pi-trash',
            rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
            message: message,
            accept: () => {
                this.userService.hardDelete(user.id).subscribe({
                    next: _ => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Hard Deleted User',
                            detail: `${user.firstName} ${user.lastName}`
                        });
                        this.softDeletedUsers$ = this.userService.getSoftDeletedUsers();
                    },
                    error: err => {
                        const message = this.errorService.format(err);
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error Hard Deleting User',
                            detail: message,
                            sticky: true
                        });
                    }
                });
            }
        });
    }
}
