import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { Devices } from "../state/device.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { BarcodeFormat } from "@zxing/library";

@Component({
    selector: 'fabx-device-add',
    templateUrl: './device-add.component.html',
    styleUrls: ['./device-add.component.scss']
})
export class DeviceAddComponent {

    error = "";

    QR_CODE = BarcodeFormat.QR_CODE;

    qrScanning = false;

    form = new FormGroup({
        mac: new FormControl('', Validators.required),
        secret: new FormControl('', Validators.required),
        name: new FormControl('', Validators.required),
        background: new FormControl('', Validators.required),
        backupBackendUrl: new FormControl('', Validators.required),
    });

    constructor(private store: Store, private errorHandler: ErrorService) { }

    enableQrScanning() {
        this.qrScanning = true;
    }

    onQrSuccess(event: string) {
        console.log("QR Code Success:", event);

        const parts = event.split("\n");

        if (parts.length != 2) {
            this.error = "Error reading QR Code: Not 2 parts.";
            return;
        }

        this.qrScanning = false;

        this.form.patchValue({
            mac: parts[0],
            secret: parts[1]
        });
    }

    onSubmit() {
        const mac = this.form.get('mac')!.value!;
        const secret = this.form.get('secret')!.value!;
        const name = this.form.get('name')!.value!;
        const background = this.form.get('background')!.value!;
        const backupBackendUrl = this.form.get('backupBackendUrl')!.value!;

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
