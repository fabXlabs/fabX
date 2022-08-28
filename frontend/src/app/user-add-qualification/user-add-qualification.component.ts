import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { Qualification } from "../models/qualification.model";
import { ErrorService } from "../services/error.service";
import { Users } from "../state/user.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { MessageService } from "primeng/api";

@Component({
    selector: 'fabx-user-add-qualification',
    templateUrl: './user-add-qualification.component.html',
    styleUrls: ['./user-add-qualification.component.scss'],
    providers: [MessageService]
})
export class UserAddQualificationComponent {

    error = "";

    form = new FormGroup({
        wikiName: new FormControl('', Validators.required),
        qualification: new FormControl(null, Validators.required),
    });

    @Select(FabxState.loggedInUserInstructorQualifications) qualifications$!: Observable<Qualification[]>;

    constructor(
        private store: Store,
        private messageService: MessageService,
        private errorHandler: ErrorService
    ) { }

    onSubmit() {
        this.error = "";

        const wikiName = this.form.get('wikiName')!.value!;
        const qualification = this.form.get('qualification')!.value!;

        this.store.dispatch(new Users.AddMemberQualificationAsInstructor(wikiName, qualification))
            .subscribe({
                next: _ => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Added Qualification',
                        detail: `to user with wiki name ${wikiName}`
                    });

                    this.form.patchValue({
                        "wikiName": null
                    });
                },
                error: (err: HttpErrorResponse) => {
                    this.error = this.errorHandler.format(err);
                }
            });
    }
}
