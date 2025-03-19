import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { AugmentedUser } from "../models/user.model";
import { ErrorService } from "../services/error.service";
import { HttpErrorResponse } from "@angular/common/http";
import { AugmentedDevice } from "../models/device.model";
import { Devices } from "../state/device.actions";

@Component({
    selector: 'fabx-user-add-card-identity-at-device',
    templateUrl: './user-add-card-identity-at-device.component.html',
    styleUrls: ['./user-add-card-identity-at-device.component.scss']
})
export class UserAddCardIdentityAtDeviceComponent implements OnInit, OnDestroy {

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    @Select(FabxState.devices) availableDevices$!: Observable<AugmentedDevice[]>;
    private availableDevicesSubscription: Subscription | null = null;
    devices: AugmentedDevice[] = [];

    error = "";

    form = new FormGroup({
        deviceId: new FormControl("", Validators.required)
    });

    constructor(private store: Store, private errorHandler: ErrorService) { }

    ngOnInit(): void {
        this.availableDevicesSubscription = this.availableDevices$.subscribe({
            next: value => {
                this.devices = value;
            }
        })
    }

    onSubmit() {
        const deviceId = this.form.get("deviceId")!.value!;
        const currentUser = this.store.selectSnapshot(FabxState.selectedUser)!;

        this.store.dispatch(new Devices.AddUserCardIdentity(
            deviceId,
            currentUser.id
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }


    ngOnDestroy(): void {
        this.availableDevicesSubscription?.unsubscribe();
    }
}
