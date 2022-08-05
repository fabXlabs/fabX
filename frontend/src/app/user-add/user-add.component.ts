import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngxs/store";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";

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

    constructor(private store: Store) { }

    onSubmit() {
        let firstName = this.form.get('firstName')!.value;
        let lastName = this.form.get('lastName')!.value;
        let wikiName = this.form.get('wikiName')!.value;

        this.store.dispatch(new Users.Add({
            'firstName': firstName,
            'lastName': lastName,
            'wikiName': wikiName
        })).subscribe({
            error: (err: HttpErrorResponse) => {
                console.log("error while adding user: ", err);
                this.error = `Error: ${err.statusText} (${err.status})`;
                if (err.error) {
                    this.error += ` ${JSON.stringify(err.error)}`;
                }
            }
        });
    }
}
