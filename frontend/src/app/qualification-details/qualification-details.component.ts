import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { Observable } from "rxjs";
import { Qualification } from "../models/qualification.model";
import { FabxState } from "../state/fabx-state";
import { ConfirmationService, MessageService } from "primeng/api";
import { Qualifications } from "../state/qualification.action";
import { ErrorService } from "../services/error.service";

@Component({
    selector: 'fabx-qualification-details',
    templateUrl: './qualification-details.component.html',
    styleUrls: ['./qualification-details.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class QualificationDetailsComponent {

    @Select(FabxState.selectedQualification) qualification$!: Observable<Qualification>;

    constructor(
        private store: Store,
        private confirmationService: ConfirmationService,
        private messageService: MessageService,
        private errorService: ErrorService
    ) { }

    delete() {
        const currentQualification = this.store.selectSnapshot(FabxState.selectedQualification)!;

        let message = `Are you sure you want to delete qualification ${currentQualification.name}?`

        this.confirmationService.confirm({
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            acceptButtonStyleClass: 'p-button-danger',
            acceptIcon: 'pi pi-trash',
            rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
            message: message,
            accept: () => {
                this.store.dispatch(new Qualifications.Delete(
                    currentQualification.id
                )).subscribe({
                    error: err => {
                        const message = this.errorService.format(err);
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error Deleting Qualification',
                            detail: message,
                            sticky: true
                        });
                    }
                });
            }
        });
    }
}
