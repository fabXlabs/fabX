import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { ErrorService } from "../services/error.service";
import { Select, Store } from "@ngxs/store";
import { Tools } from "../state/tool.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { IdleState, idleStates, ToolType, toolTypes } from "../models/tool.model";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { Qualification } from "../models/qualification.model";

@Component({
    selector: 'fabx-tool-add',
    templateUrl: './tool-add.component.html',
    styleUrls: ['./tool-add.component.scss']
})
export class ToolAddComponent {

    error = "";

    form = new FormGroup({
        name: new FormControl('', Validators.required),
        wikiLink: new FormControl('', Validators.required),
        requiredQualifications: new FormControl(null, Validators.required),
        type: new FormControl(null, Validators.required),
        requires2FA: new FormControl(false, Validators.required),
        time: new FormControl(300, Validators.required),
        idleState: new FormControl(null, Validators.required),
    });

    types: ToolType[] = toolTypes;
    idleStates: IdleState[] = idleStates;

    @Select(FabxState.qualifications) qualifications$!: Observable<Qualification[]>;

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onSubmit() {
        const name = this.form.get('name')!.value!;
        const type = this.form.get('type')!.value!;
        const requires2FA = this.form.get('requires2FA')!.value!;
        const time = this.form.get('time')!.value!;
        const idleState = this.form.get('idleState')!.value!;
        const wikiLink = this.form.get('wikiLink')!.value!;
        const requiredQualifications = this.form.get('requiredQualifications')!.value!;

        this.store.dispatch(new Tools.Add({
            name: name,
            type: type,
            requires2FA: requires2FA,
            time: time,
            idleState: idleState,
            wikiLink: wikiLink,
            requiredQualifications: requiredQualifications
        })).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    get requires2FAForm() {
        return this.form.controls['requires2FA'] as FormControl;
    }
}
