import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedDevice } from "../models/device.model";
import { ErrorService } from "../services/error.service";
import { Devices } from "../state/device.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-device-change-desired-firmware-version',
    templateUrl: './device-change-desired-firmware-version.component.html',
    styleUrls: ['./device-change-desired-firmware-version.component.scss']
})
export class DeviceChangeDesiredFirmwareVersionComponent implements OnInit, OnDestroy {

    error = "";

    form = new FormGroup({
        desiredFirmwareVersion: new FormControl('', Validators.required),
    });

    @Select(FabxState.selectedDevice) device$!: Observable<AugmentedDevice>;
    private selectedDeviceSubscription: Subscription | null = null;

    constructor(private store: Store, private errorHandler: ErrorService) {}

    ngOnInit(): void {
        this.selectedDeviceSubscription = this.device$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        desiredFirmwareVersion: value.desiredFirmwareVersion
                    });
                }
            }
        });
    }

    onSubmit() {
        const desiredFirmwareVersion = this.form.get('desiredFirmwareVersion')!.value!;

        const currentDevice = this.store.selectSnapshot(FabxState.selectedDevice)!;

        this.store.dispatch(new Devices.ChangeDesiredFirmwareVersion(
            currentDevice.id,
            desiredFirmwareVersion
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    ngOnDestroy(): void {
        if (this.selectedDeviceSubscription) {
            this.selectedDeviceSubscription.unsubscribe();
        }
    }
}
