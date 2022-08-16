import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { Devices } from "../state/device.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-device-add',
    templateUrl: './device-add.component.html',
    styleUrls: ['./device-add.component.scss']
})
export class DeviceAddComponent {

    error = "";

    form = new FormGroup({
        mac: new FormControl('', Validators.required),
        secret: new FormControl('', Validators.required),
        name: new FormControl('', Validators.required),
        background: new FormControl('', Validators.required),
        backupBackendUrl: new FormControl('', Validators.required),
    });

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const mac = this.form.get('mac')!.value;
        const secret = this.form.get('secret')!.value;
        const name = this.form.get('name')!.value;
        const background = this.form.get('background')!.value;
        const backupBackendUrl = this.form.get('backupBackendUrl')!.value;

        this.store.dispatch(new Devices.Add({
            mac: mac,
            secret: secret,
            name: name,
            background: background,
            backupBackendUrl: backupBackendUrl,
        })).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
