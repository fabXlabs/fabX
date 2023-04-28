import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Observable, Subscription } from "rxjs";
import { AugmentedTool, IdleState, idleStates, ToolType, toolTypes } from "../models/tool.model";
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
export class ToolChangeDetailsComponent implements OnInit, OnDestroy {

    error = "";

    form = new FormGroup({
        name: new FormControl('', Validators.required),
        wikiLink: new FormControl('', Validators.required),
        requiredQualifications: new FormControl<string[]>([], Validators.required),
        requires2FA: new FormControl<boolean>(false, Validators.required),

        enabled: new FormControl<boolean>(true, Validators.required),
        notes: new FormControl(''),

        type: new FormControl<ToolType | null>(null, Validators.required),
        time: new FormControl(0, Validators.required),
        idleState: new FormControl<IdleState | null>(null, Validators.required),
    });

    types: ToolType[] = toolTypes;
    idleStates: IdleState[] = idleStates;

    @Select(FabxState.qualifications) qualifications$!: Observable<Qualification[]>;

    @Select(FabxState.selectedTool) tool$!: Observable<AugmentedTool>;
    private selectedToolSubscription: Subscription | null = null;

    constructor(private store: Store, private errorHandler: ErrorService) {}

    ngOnInit() {
        this.selectedToolSubscription = this.tool$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        name: value.name,
                        wikiLink: value.wikiLink,
                        requiredQualifications: value.requiredQualifications.map(q => q.id),
                        requires2FA: value.requires2FA,

                        enabled: Boolean(value.enabled),
                        notes: value.notes,

                        type: value.type,
                        time: value.time,
                        idleState: value.idleState,
                    });
                }
            }
        });
    }

    get enabledForm() {
        return this.form.controls['enabled'] as FormControl;
    }

    get requires2FAForm() {
        return this.form.controls['requires2FA'] as FormControl;
    }

    onSubmit() {
        const name = this.form.get('name')!.value!;
        const wikiLink = this.form.get('wikiLink')!.value!;
        const requiredQualifications = this.form.get('requiredQualifications')!.value!;
        const requires2FA = this.form.get('requires2FA')!.value!;

        const enabled = this.form.get('enabled')!.value!;
        let notes = this.form.get('notes')!.value;
        if (!notes) {
            notes = null
        }

        const type = this.form.get('type')!.value!;
        const time = this.form.get('time')!.value!;
        const idleState = this.form.get('idleState')!.value!;

        const currentTool = this.store.selectSnapshot(FabxState.selectedTool)!;

        let nameChange: ChangeableValue<string> | null = null;
        if (name != currentTool.name) {
            nameChange = {
                newValue: name
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

        let requires2FAChange: ChangeableValue<boolean> | null = null;
        if (requires2FA != currentTool.requires2FA) {
            requires2FAChange = {
                newValue: requires2FA
            }
        }

        let enabledChange: ChangeableValue<boolean> | null = null;
        if (enabled != currentTool.enabled) {
            enabledChange = {
                newValue: enabled
            }
        }

        let notesChange: ChangeableValue<string | null> | null = null;
        if (notes != currentTool.notes) {
            notesChange = {
                newValue: notes
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

        this.store.dispatch(new Tools.ChangeDetails(
            currentTool.id,
            {
                name: nameChange,
                wikiLink: wikiLinkChange,
                requiredQualifications: requiredQualificationsChange,
                requires2FA: requires2FAChange,
                enabled: enabledChange,
                notes: notesChange,
                type: typeChange,
                time: timeChange,
                idleState: idleStateChange,
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    ngOnDestroy() {
        if (this.selectedToolSubscription) {
            this.selectedToolSubscription.unsubscribe();
        }
    }
}
