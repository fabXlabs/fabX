import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedTool } from "../models/tool.model";
import { ConfirmationService, MessageService } from "primeng/api";
import { Tools } from "../state/tool.actions";
import { ErrorService } from "../services/error.service";
import { User } from "../models/user.model";

@Component({
    selector: 'fabx-tool-details',
    templateUrl: './tool-details.component.html',
    styleUrls: ['./tool-details.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class ToolDetailsComponent {

    @Select(FabxState.selectedTool) tool$!: Observable<AugmentedTool>;
    @Select(FabxState.loggedInUser) loggedInUser$!: Observable<User>;

    constructor(
        private store: Store,
        private confirmationService: ConfirmationService,
        private messageService: MessageService,
        private errorService: ErrorService
    ) { }

    delete() {
        const currentTool = this.store.selectSnapshot(FabxState.selectedTool)!;

        let message = `Are you sure you want to delete tool ${currentTool.name}?`

        this.confirmationService.confirm({
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            acceptButtonStyleClass: 'p-button-danger',
            acceptIcon: 'pi pi-trash',
            rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
            message: message,
            accept: () => {
                this.store.dispatch(new Tools.Delete(
                    currentTool.id
                )).subscribe({
                    error: err => {
                        const message = this.errorService.format(err);
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error Deleting Tool',
                            detail: message,
                            sticky: true
                        });
                    }
                });
            }
        });
    }
}
