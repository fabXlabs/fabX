import { Component } from '@angular/core';
import { Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { Devices } from "../state/device.actions";
import { FabxState } from "../state/fabx-state";
import { HttpErrorResponse } from "@angular/common/http";
import { FormControl, FormGroup } from "@angular/forms";

@Component({
    selector: 'fabx-device-change-thumbnail',
    templateUrl: './device-change-thumbnail.component.html',
    styleUrls: ['./device-change-thumbnail.component.scss']
})
export class DeviceChangeThumbnailComponent {

    error = "";

    form = new FormGroup({
        file: new FormControl('')
    });

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onFileSelected(event: Event) {
        const target = event.target as HTMLInputElement;
        const files = target.files as FileList;
        const file = files[0];

        const currentDevice = this.store.selectSnapshot(FabxState.selectedDevice)!;

        if (file) {
            console.debug("onFileSelected: {}", file)

            this.store.dispatch(new Devices.ChangeThumbnail(currentDevice.id, file))
                .subscribe({
                    error: (err: HttpErrorResponse) => {
                        this.error = this.errorHandler.format(err);
                    }
                });
        }
    }
}
