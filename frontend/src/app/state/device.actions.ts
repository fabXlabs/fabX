export namespace Devices {
    export class GetAll {
        static readonly type = "[fabX Devices] Get All"
    }

    export class GetById {
        static readonly type = "[fabX Devices] Get By Id"

        constructor(public id: string) {}
    }

    export class AttachTool {
        static readonly type = "[fabX Devices] Attach Tool"

        constructor(public deviceId: string, public pin: number, public toolId: string) {}
    }

    export class DetachTool {
        static readonly type = "[fabX Devices] Detach Tool"

        constructor(public deviceId: string, public pin: number) {}
    }
}
