import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedDevice } from "../models/device.model";

@Component({
    selector: 'fabx-device-details',
    templateUrl: './device-details.component.html',
    styleUrls: ['./device-details.component.scss']
})
export class DeviceDetailsComponent {

    @Select(FabxState.selectedDevice) device$!: Observable<AugmentedDevice>;

    constructor(private store: Store) { }
}
