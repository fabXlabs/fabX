import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import { Tool, ToolCreationDetails } from "../models/tool.model";

@Injectable({
    providedIn: 'root'
})
export class ToolService {

    private baseUrl = environment.baseUrl;

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) { }

    public getAllTools(): Observable<Tool[]> {
        return this.http.get<Tool[]>(`${this.baseUrl}/tool`, this.authService.getOptions()).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<Tool> {
        return this.http.get<Tool>(`${this.baseUrl}/tool/${id}`, this.authService.getOptions());
    }

    public addTool(details: ToolCreationDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/tool`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }
}
