import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Select, Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { Qualifications } from "../state/qualification.action";
import { HttpErrorResponse } from "@angular/common/http";
import { Observable, Subscription } from "rxjs";
import { Qualification } from "../models/qualification.model";
import { FabxState } from "../state/fabx-state";
import { ChangeableValue } from "../models/changeable-value";

@Component({
    selector: 'fabx-qualification-change-details',
    templateUrl: './qualification-change-details.component.html',
    styleUrls: ['./qualification-change-details.component.scss']
})
export class QualificationChangeDetailsComponent implements OnInit, OnDestroy {

    error = "";

    form = new FormGroup({
        name: new FormControl('', Validators.required),
        description: new FormControl('', Validators.required),
        colour: new FormControl('#FFFFFF', Validators.required),
        orderNr: new FormControl(100, Validators.required),
    });

    @Select(FabxState.selectedQualification) qualification$!: Observable<Qualification>;
    private selectedQualificationSubscription: Subscription | null = null;

    constructor(private store: Store, private errorHandler: ErrorService) {}

    ngOnInit() {
        this.selectedQualificationSubscription = this.qualification$.subscribe({
            next: value => {
                if (value) {
                    this.form.patchValue({
                        name: value.name,
                        description: value.description,
                        colour: value.colour,
                        orderNr: value.orderNr,
                    });
                }
            }
        });
    }

    onSubmit() {
        const name = this.form.get('name')!.value;
        const description = this.form.get('description')!.value;
        const colour = this.form.get('colour')!.value;
        const orderNr = this.form.get('orderNr')!.value;

        const currentQualification = this.store.selectSnapshot(FabxState.selectedQualification)!;

        let nameChange: ChangeableValue<string> | null = null;
        if (name != currentQualification.name) {
            nameChange = {
                newValue: name!
            }
        }

        let descriptionChange: ChangeableValue<string> | null = null;
        if (description != currentQualification.description) {
            descriptionChange = {
                newValue: description!
            }
        }

        let colourChange: ChangeableValue<string> | null = null;
        if (colour != currentQualification.colour) {
            colourChange = {
                newValue: colour!
            }
        }

        let orderNrChange: ChangeableValue<number> | null = null;
        if (orderNr != currentQualification.orderNr) {
            orderNrChange = {
                newValue: orderNr!
            }
        }

        this.store.dispatch(new Qualifications.ChangeDetails(
            currentQualification.id,
            {
                name: nameChange,
                colour: colourChange,
                description: descriptionChange,
                orderNr: orderNrChange
            }
        )).subscribe({
            error: (err: HttpErrorResponse) => {
                this.error = this.errorHandler.format(err);
            }
        });
    }

    ngOnDestroy() {
        if (this.selectedQualificationSubscription) {
            this.selectedQualificationSubscription.unsubscribe();
        }
    }
}
