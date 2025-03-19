import { Component, Pipe, PipeTransform } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { LoadingStateTag } from "../state/loading-state.model";
import { Device } from "../models/device.model";
import { Tool } from "../models/tool.model";
import { DeviceService } from "../services/device.service";

@Component({
    selector: 'fabx-devices',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.scss']
})
export class DevicesComponent {

    @Select(FabxState.devicesLoadingState) loading$!: Observable<LoadingStateTag>;
    @Select(FabxState.devices) devices$!: Observable<Device[]>;

    constructor(private store: Store, private deviceService: DeviceService) { }

    thumbnailUrl(id: string) {
        return this.deviceService.thumbnailUrl(id);
    }
}

@Pipe({ name: 'attachedToolNames' })
export class AttachedToolNames implements PipeTransform {
    transform(value: Record<number, Tool>): string {
        const tools: Tool[] = Object.values(value);
        return tools.map(tool => tool.name).join(", ");
    }
}
