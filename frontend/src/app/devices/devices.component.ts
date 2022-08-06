import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { LoadingStateTag } from "../state/loading-state.model";
import { Device } from "../models/device.model";

@Component({
    selector: 'fabx-devices',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.scss']
})
export class DevicesComponent {

    @Select(FabxState.devicesLoadingState) loading$!: Observable<LoadingStateTag>;
    @Select(FabxState.devices) devices$!: Observable<Device[]>;

    constructor(private store: Store) { }
}
