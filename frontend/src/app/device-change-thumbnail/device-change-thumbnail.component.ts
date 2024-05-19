import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { Devices } from "../state/device.actions";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedDevice } from "../models/device.model";
import { HttpErrorResponse } from "@angular/common/http";
import { FormControl, FormGroup } from "@angular/forms";

@Component({
    selector: 'fabx-device-change-thumbnail',
    templateUrl: './device-change-thumbnail.component.html',
    styleUrls: ['./device-change-thumbnail.component.scss']
})
export class DeviceChangeThumbnailComponent {

    error = "";
    success = false;

    form = new FormGroup({
        file: new FormControl('')
    });

    @Select(FabxState.selectedDevice) device$!: Observable<AugmentedDevice>;
    private selectedDeviceSubscription: Subscription | null = null;

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onFileSelected(event: Event) {
        this.success = false;

        const target = event.target as HTMLInputElement;
        const files = target.files as FileList;
        const file = files[0];

        const currentDevice = this.store.selectSnapshot(FabxState.selectedDevice)!;

        if (file) {
            console.debug("onFileSelected: {}", file)

            this.store.dispatch(new Devices.ChangeThumbnail(currentDevice.id, file))
                .subscribe({
                    next: _ => {
                        this.form.reset();
                        this.success = true;
                    },
                    error: (err: HttpErrorResponse) => {
                        this.error = this.errorHandler.format(err);
                    }
                });
        }
    }
}
