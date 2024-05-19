import { DeviceCreationDetails, DeviceDetails } from "../models/device.model";

export namespace Devices {
    export class GetAll {
        static readonly type = "[fabX Devices] Get All"
    }

    export class GetById {
        static readonly type = "[fabX Devices] Get By Id"

        constructor(public id: string) {}
    }

    export class Add {
        static readonly type = "[fabX Devices] Add"

        constructor(public details: DeviceCreationDetails) {}
    }

    export class ChangeDetails {
        static readonly type = "[fabX Devices] Change Details"

        constructor(public id: string, public details: DeviceDetails) {}
    }

    export class ChangeDesiredFirmwareVersion {
        static readonly type = "[fabX Devices] Change Desired Firmware Version"

        constructor(public id: string, public desiredFirmwareVersion: string) {}
    }

    export class AttachTool {
        static readonly type = "[fabX Devices] Attach Tool"

        constructor(public deviceId: string, public pin: number, public toolId: string) {}
    }

    export class DetachTool {
        static readonly type = "[fabX Devices] Detach Tool"

        constructor(public deviceId: string, public pin: number) {}
    }

    export class UnlockTool {
        static readonly type = "[fabX Devices] Unlock Tool"

        constructor(public deviceId: string, public toolId: string) {}
    }

    export class Restart {
        static readonly type = "[fabX Devices] Restart"

        constructor(public deviceId: string) {}
    }

    export class UpdateFirmware {
        static readonly type = "[fabX Devices] Update Firmware"

        constructor(public deviceId: string) {}
    }

    export class Delete {
        static readonly type = "[fabX Devices] Delete"

        constructor(public id: string) {}
    }

    export class ChangeThumbnail {
        static readonly type = "[fabX Devices] Change Thumbnail"

        constructor(public id: string, public file: File) {}
    }
}
