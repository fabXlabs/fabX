import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedDevice } from "../models/device.model";
import { Devices } from "../state/device.actions";
import { ConfirmationService } from "primeng/api";

@Component({
    selector: 'fabx-device-details',
    templateUrl: './device-details.component.html',
    styleUrls: ['./device-details.component.scss'],
    providers: [ConfirmationService]
})
export class DeviceDetailsComponent {

    @Select(FabxState.selectedDevice) device$!: Observable<AugmentedDevice>;

    constructor(private store: Store, private confirmationService: ConfirmationService) { }

    detachTool(pin: string) {
        this.store.dispatch(new Devices.DetachTool(
            this.store.selectSnapshot(FabxState.selectedDevice)!.id,
            parseInt(pin)
        ));
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
                ));
            }
        });
    }
}
