import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngxs/store";

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
        console.log("submit!");
    }
}
