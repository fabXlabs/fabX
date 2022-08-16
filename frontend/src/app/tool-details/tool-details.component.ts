import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedTool } from "../models/tool.model";
import { ConfirmationService } from "primeng/api";
import { Tools } from "../state/tool.actions";

@Component({
    selector: 'fabx-tool-details',
    templateUrl: './tool-details.component.html',
    styleUrls: ['./tool-details.component.scss'],
    providers: [ConfirmationService]
})
export class ToolDetailsComponent {

    @Select(FabxState.selectedTool) tool$!: Observable<AugmentedTool>;

    constructor(private store: Store, private confirmationService: ConfirmationService) { }

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
                ));
            }
        });
    }
}
