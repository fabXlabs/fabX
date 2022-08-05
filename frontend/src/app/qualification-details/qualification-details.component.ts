import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { Observable } from "rxjs";
import { Qualification } from "../models/qualification.model";
import { FabxState } from "../state/fabx-state";

@Component({
    selector: 'fabx-qualification-details',
    templateUrl: './qualification-details.component.html',
    styleUrls: ['./qualification-details.component.scss']
})
export class QualificationDetailsComponent {

    @Select(FabxState.selectedQualification) qualification$!: Observable<Qualification>;

    constructor(private store: Store) { }
}
