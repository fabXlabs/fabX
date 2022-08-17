import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedDevice } from "../models/device.model";
import { Devices } from "../state/device.actions";
import { ConfirmationService, MessageService } from "primeng/api";
import { ErrorService } from "../services/error.service";

@Component({
    selector: 'fabx-device-details',
    templateUrl: './device-details.component.html',
    styleUrls: ['./device-details.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class DeviceDetailsComponent {

    @Select(FabxState.selectedDevice) device$!: Observable<AugmentedDevice>;

    constructor(
        private store: Store,
        private confirmationService: ConfirmationService,
        private messageService: MessageService,
        private errorService: ErrorService
    ) { }

    detachTool(pin: string) {
        this.store.dispatch(new Devices.DetachTool(
            this.store.selectSnapshot(FabxState.selectedDevice)!.id,
            parseInt(pin)
        )).subscribe({
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Detaching Tool',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    unlock(toolId: string) {
        this.store.dispatch(new Devices.UnlockTool(
            this.store.selectSnapshot(FabxState.selectedDevice)!.id,
            toolId
        )).subscribe({
            next: _ => {
                this.messageService.add({ severity: 'success', summary: 'Unlocked Tool', detail: 'Yay!' });
            },
            error: err => {
                const message = this.errorService.format(err);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error Unlocking Tool',
                    detail: message,
                    sticky: true
                });
            }
        });
    }

    delete() {
        const currentDevice = this.store.selectSnapshot(FabxState.selectedDevice)!;

        let message = `Are you sure you want to delete device ${currentDevice.name}?`

        this.confirmationService.confirm({
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            acceptButtonStyleClass: 'p-button-danger',
            acceptIcon: 'pi pi-trash',
            rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
            message: message,
            accept: () => {
                this.store.dispatch(new Devices.Delete(
                    currentDevice.id
                )).subscribe({
                    error: err => {
                        const message = this.errorService.format(err);
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error Deleting Device',
                            detail: message,
                            sticky: true
                        });
                    }
                });
            }
        });
    }
}
