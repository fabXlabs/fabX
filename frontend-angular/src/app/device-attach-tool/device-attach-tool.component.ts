import { Component } from '@angular/core';
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { Tool } from "../models/tool.model";
import { Select, Store } from "@ngxs/store";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Device } from "../models/device.model";
import { ErrorService } from "../services/error.service";
import { Devices } from "../state/device.actions";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
    selector: 'fabx-device-attach-tool',
    templateUrl: './device-attach-tool.component.html',
    styleUrls: ['./device-attach-tool.component.scss']
})
export class DeviceAttachToolComponent {

    error = "";

    form = new FormGroup({
        toolId: new FormControl(null, Validators.required),
        pin: new FormControl(0, Validators.required),
    });

    @Select(FabxState.selectedDevice) device$!: Observable<Device>;
    @Select(FabxState.tools) tools$!: Observable<Tool[]>;

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const deviceId = this.store.selectSnapshot(FabxState.selectedDevice)!.id;
        const pin = this.form.get('pin')!.value!;
        const toolId = this.form.get('toolId')!.value!;

        this.store.dispatch(new Devices.AttachTool(
            deviceId,
            pin,
            toolId
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
