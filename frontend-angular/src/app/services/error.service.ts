import { Injectable } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";
import { Error } from "../models/error.model";

@Injectable({
    providedIn: 'root'
})
export class ErrorService {

    constructor() { }

    format(err: HttpErrorResponse): string {
        let formattedError: string;
        if (err.error) {
            try {
                const e: Error = JSON.parse(err.error);
                formattedError = `Error: ${e.message} (${e.type}, ${JSON.stringify(e.parameters)})  (${err.statusText} ${err.status}; ${e.correlationId})`;
            } catch (e) {
                formattedError = `Error: ${err.statusText} (${err.status})`;
                if (err.error) {
                    formattedError += ` ${JSON.stringify(err.error)}`;
                }
            }
        } else {
            formattedError = `Error: ${err.statusText} (${err.status})`;
        }
        return formattedError;
    }
}
