import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import { Device } from "../models/device.model";

@Injectable({
    providedIn: 'root'
})
export class DeviceService {

    private baseUrl = environment.baseUrl;

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) { }

    public getAllDevices(): Observable<Device[]> {
        return this.http.get<Device[]>(`${this.baseUrl}/device`, this.authService.getOptions()).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<Device> {
        return this.http.get<Device>(`${this.baseUrl}/device/${id}`, this.authService.getOptions());
    }
}
