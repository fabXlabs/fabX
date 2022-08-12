import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Observable, Subscription } from "rxjs";
import { AugmentedTool, IdleState, ToolType, toolTypes, idleStates } from "../models/tool.model";
import { FabxState } from "../state/fabx-state";
import { Select, Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { ChangeableValue } from "../models/changeable-value";
import { Tools } from "../state/tool.actions";
import { HttpErrorResponse } from "@angular/common/http";
import { Qualification } from "../models/qualification.model";

@Component({
    selector: 'fabx-tool-change-details',
    templateUrl: './tool-change-details.component.html',
    styleUrls: ['./tool-change-details.component.scss']
})
export class ToolChangeDetailsComponent {

    error = "";

    form = new FormGroup({
        name: new FormControl('', Validators.required),
        wikiLink: new FormControl('', Validators.required),
        enabled: new FormControl(true, Validators.required),
        requiredQualifications: new FormControl(null, Validators.required),

        type: new FormControl(null, Validators.required),
        time: new FormControl('0', Validators.required),
        idleState: new FormControl('', Validators.required),
    });

    types: ToolType[] = toolTypes;
    idleStates: IdleState[] = idleStates;

    @Select(FabxState.qualifications) qualifications$!: Observable<Qualification[]>;

    @Select(FabxState.selectedTool) tool$!: Observable<AugmentedTool>;
    private selectedToolSubscription: Subscription;

    constructor(private store: Store, private errorHandler: ErrorService) {
        this.selectedToolSubscription = this.tool$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        name: value.name,
                        type: value.type,
                        time: value.time,
                        idleState: value.idleState,
                        enabled: value.enabled,
                        wikiLink: value.wikiLink,
                        requiredQualifications: value.requiredQualifications.map(q => q.id),
                    });
                }
            }
        });
    }

    get enabledForm() {
        return this.form.controls['enabled'] as FormControl;
    }

    onSubmit() {
        const name = this.form.get('name')!.value;
        const type = this.form.get('type')!.value;
        const time = this.form.get('time')!.value;
        const idleState = this.form.get('idleState')!.value;
        const enabled = this.form.get('enabled')!.value;
        const wikiLink = this.form.get('wikiLink')!.value;
        const requiredQualifications = this.form.get('requiredQualifications')!.value;

        const currentTool = this.store.selectSnapshot(FabxState.selectedTool)!;

        let nameChange: ChangeableValue<string> | null = null;
        if (name != currentTool.name) {
            nameChange = {
                newValue: name
            }
        }

        let typeChange: ChangeableValue<ToolType> | null = null;
        if (type != currentTool.type) {
            typeChange = {
                newValue: type
            }
        }

        let timeChange: ChangeableValue<number> | null = null;
        if (time != currentTool.time) {
            timeChange = {
                newValue: time
            }
        }

        let idleStateChange: ChangeableValue<IdleState> | null = null;
        if (idleState != currentTool.idleState) {
            idleStateChange = {
                newValue: idleState
            }
        }

        let enabledChange: ChangeableValue<boolean> | null = null;
        if (enabled != currentTool.enabled) {
            enabledChange = {
                newValue: enabled
            }
        }

        let wikiLinkChange: ChangeableValue<string> | null = null;
        if (wikiLink != currentTool.wikiLink) {
            wikiLinkChange = {
                newValue: wikiLink
            }
        }

        let requiredQualificationsChange: ChangeableValue<string[]> | null = null;
        if (currentTool.requiredQualifications.length != requiredQualifications.length ||
            !currentTool.requiredQualifications.map(q => q.id).every((v, i) => v === requiredQualifications[i])) {
            requiredQualificationsChange = {
                newValue: requiredQualifications
            }
        }

        this.store.dispatch(new Tools.ChangeDetails(
            currentTool.id,
            {
                name: nameChange,
                type: typeChange,
                time: timeChange,
                idleState: idleStateChange,
                enabled: enabledChange,
                wikiLink: wikiLinkChange,
                requiredQualifications: requiredQualificationsChange,
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }
}
