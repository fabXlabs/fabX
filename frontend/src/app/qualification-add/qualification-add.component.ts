import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngxs/store";
import { Qualifications } from "../state/qualification.action";
import { HttpErrorResponse } from "@angular/common/http";
import { Error } from "../models/error.model";

@Component({
    selector: 'fabx-qualification-add',
    templateUrl: './qualification-add.component.html',
    styleUrls: ['./qualification-add.component.scss']
})
export class QualificationAddComponent {

    error = "";

    form = new FormGroup({
        name: new FormControl('', Validators.required),
        description: new FormControl('', Validators.required),
        colour: new FormControl('', Validators.required),
        orderNr: new FormControl('100', Validators.required),
    });

    constructor(private store: Store) { }

    onSubmit() {
        const name = this.form.get('name')!.value;
        const description = this.form.get('description')!.value;
        const colour = this.form.get('colour')!.value;
        const orderNr = this.form.get('orderNr')!.value;

        this.store.dispatch(new Qualifications.Add({
            name: name,
            colour: colour,
            description: description,
            orderNr: orderNr
        })).subscribe({
            // TODO refactor duplicate error handler
            error: (err: HttpErrorResponse) => {
                if (err.error) {
                    try {
                        const e: Error = JSON.parse(err.error);
                        this.error = `Error: ${e.message} (${e.type}, ${JSON.stringify(e.parameters)})  (${err.statusText} ${err.status} ${e.correlationId})`;
                    } catch (e) {
                        this.error = `Error: ${err.statusText} (${err.status})`;
                        if (err.error) {
                            this.error += ` ${JSON.stringify(err.error)}`;
                        }
                    }
                }
            }
        });
    }
}
