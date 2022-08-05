import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "./auth.service";
import { Observable, retry } from "rxjs";
import { Qualification } from "../models/qualification.model";

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
}
