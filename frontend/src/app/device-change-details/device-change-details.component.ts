import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { FabxState } from "../state/fabx-state";
import { Select, Store } from "@ngxs/store";
import { Observable, Subscription } from "rxjs";
import { AugmentedDevice } from "../models/device.model";
import { ErrorService } from "../services/error.service";
import { ChangeableValue } from "../models/changeable-value";
import { Devices } from "../state/device.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-device-change-details',
    templateUrl: './device-change-details.component.html',
    styleUrls: ['./device-change-details.component.scss']
})
export class DeviceChangeDetailsComponent implements OnInit, OnDestroy {

    error = "";

    form = new FormGroup({
        name: new FormControl('', Validators.required),
        background: new FormControl('', Validators.required),
        backupBackendUrl: new FormControl('', Validators.required),
    });

    @Select(FabxState.selectedDevice) device$!: Observable<AugmentedDevice>;
    private selectedDeviceSubscription: Subscription | null = null;

    constructor(private store: Store, private errorHandler: ErrorService) {}

    ngOnInit() {
        this.selectedDeviceSubscription = this.device$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        name: value.name,
                        background: value.background,
                        backupBackendUrl: value.backupBackendUrl,
                    });
                }
            }
        });
    }

    onSubmit() {
        const name = this.form.get('name')!.value!;
        const background = this.form.get('background')!.value!;
        const backupBackendUrl = this.form.get('backupBackendUrl')!.value!;

        const currentDevice = this.store.selectSnapshot(FabxState.selectedDevice)!;

        let nameChange: ChangeableValue<string> | null = null;
        if (name != currentDevice.name) {
            nameChange = {
                newValue: name
            }
        }

        let backgroundChange: ChangeableValue<string> | null = null;
        if (background != currentDevice.background) {
            backgroundChange = {
                newValue: background
            }
        }

        let backupBackendUrlChange: ChangeableValue<string> | null = null;
        if (backupBackendUrl != currentDevice.backupBackendUrl) {
            backupBackendUrlChange = {
                newValue: backupBackendUrl
            }
        }

        this.store.dispatch(new Devices.ChangeDetails(
            currentDevice.id,
            {
                name: nameChange,
                background: backgroundChange,
                backupBackendUrl: backupBackendUrlChange,
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    ngOnDestroy() {
        this.selectedDeviceSubscription?.unsubscribe();
    }
}
