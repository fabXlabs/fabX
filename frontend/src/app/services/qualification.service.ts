import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import { Qualification, QualificationCreationDetails, QualificationDetails } from "../models/qualification.model";

@Injectable({
    providedIn: 'root'
})
export class QualificationService {

    private baseUrl = environment.baseUrl;

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) { }

    public getAllQualifications(): Observable<Qualification[]> {
        return this.http.get<Qualification[]>(`${this.baseUrl}/qualification`, this.authService.getOptions()).pipe(
            retry(3)
        );
    }

    public getById(id: string): Observable<Qualification> {
        return this.http.get<Qualification>(`${this.baseUrl}/qualification/${id}`, this.authService.getOptions());
    }

    public addQualification(details: QualificationCreationDetails): Observable<string> {
        return this.http.post(
            `${this.baseUrl}/qualification`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public changeDetails(id: string, details: QualificationDetails): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/qualification/${id}`,
            details,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }

    public deleteQualification(id: string): Observable<string> {
        return this.http.delete(
            `${this.baseUrl}/qualification/${id}`,
            {
                ...this.authService.getOptions(),
                responseType: 'text'
            }
        );
    }
}
