import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Observable, retry } from "rxjs";
import { Tool, ToolCreationDetails, ToolDetails } from "../models/tool.model";

@Injectable({
    providedIn: 'root'
})
export class ToolService {

    private baseUrl = environment.baseUrl;

    constructor(
        private http: HttpClient
    ) { }

    public getAllTools(): Observable<Tool[]> {
        return this.http.get<Tool[]>(`${this.baseUrl}/tool`).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<Tool> {
        return this.http.get<Tool>(`${this.baseUrl}/tool/${id}`);
    }

    public addTool(details: ToolCreationDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/tool`,
            details,
            { responseType: 'text' }
        );
    }

    public changeDetails(id: string, details: ToolDetails): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/tool/${id}`,
            details,
            { responseType: 'text' }
        );
    }

    public deleteTool(id: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/tool/${id}`,
            { responseType: 'text' }
        );
    }

    public changeThumbnail(id: string, file: File): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/tool/${id}/thumbnail`,
            file,
            { responseType: 'text' }
        );
    }

    public thumbnailUrl(id: string): string {
        return `${this.baseUrl}/tool/${id}/thumbnail`;
    }
}
