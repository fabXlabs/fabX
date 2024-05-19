import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Observable, retry } from "rxjs";
import {
    DesiredFirmwareVersion,
    Device,
    DeviceCreationDetails,
    DeviceDetails,
    ToolAttachmentDetails,
    ToolUnlockDetails
} from "../models/device.model";

@Injectable({
    providedIn: 'root'
})
export class DeviceService {

    private baseUrl = environment.baseUrl;

    constructor(
        private http: HttpClient
    ) { }

    public getAllDevices(): Observable<Device[]> {
        return this.http.get<Device[]>(`${this.baseUrl}/device`).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<Device> {
        return this.http.get<Device>(`${this.baseUrl}/device/${id}`);
    }

    public addDevice(details: DeviceCreationDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/device`,
            details,
            { responseType: 'text' }
        );
    }

    public changeDetails(id: string, details: DeviceDetails): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/device/${id}`,
            details,
            { responseType: 'text' }
        );
    }

    public changeDesiredFirmwareVersion(id: string, desiredFirmwareVersion: string): Observable<string> {
        const details: DesiredFirmwareVersion = {
            desiredFirmwareVersion: desiredFirmwareVersion
        }
        return this.http.put(
            `${this.baseUrl}/device/${id}/desired-firmware-version`,
            details,
            { responseType: 'text' }
        );
    }

    public attachTool(deviceId: string, pin: number, toolId: string): Observable<string> {
        const details: ToolAttachmentDetails = {
            toolId: toolId
        };

        return this.http.put(
            `${this.baseUrl}/device/${deviceId}/attached-tool/${pin}`,
            details,
            { responseType: 'text' }
        );
    }

    public detachTool(deviceId: string, pin: number): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/device/${deviceId}/attached-tool/${pin}`,
            { responseType: 'text' }
        );
    }

    public unlockTool(deviceId: string, details: ToolUnlockDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/device/${deviceId}/unlock-tool`,
            details,
            { responseType: 'text' }
        );
    }

    public restartDevice(deviceId: string): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/device/${deviceId}/restart`,
            null,
            { responseType: 'text' }
        );
    }

    public updateFirmware(deviceId: string): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/device/${deviceId}/update-firmware`,
            null,
            { responseType: 'text' }
        );
    }

    public deleteDevice(id: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/device/${id}`,
            { responseType: 'text' }
        );
    }

    public changeThumbnail(id: string, file: File): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/device/${id}/thumbnail`,
            file,
            { responseType: 'text' }
        );
    }

    public thumbnailUrl(id: string): string {
        return `${this.baseUrl}/device/${id}/thumbnail`;
    }
}
