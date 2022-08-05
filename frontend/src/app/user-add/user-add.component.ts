import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngxs/store";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorService } from "../services/error.service";

@Component({
    selector: 'fabx-user-add',
    templateUrl: './user-add.component.html',
    styleUrls: ['./user-add.component.scss']
})
export class UserAddComponent {

    error = "";

    form = new FormGroup({
        firstName: new FormControl('', Validators.required),
        lastName: new FormControl('', Validators.required),
        wikiName: new FormControl('', Validators.required),
    });

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const firstName = this.form.get('firstName')!.value;
        const lastName = this.form.get('lastName')!.value;
        const wikiName = this.form.get('wikiName')!.value;

        this.store.dispatch(new Users.Add({
            firstName: firstName,
            lastName: lastName,
            wikiName: wikiName
        })).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
