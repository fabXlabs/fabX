import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { Qualification } from "../models/qualification.model";
import { LoadingStateTag } from "../state/loading-state.model";

@Component({
    selector: 'fabx-qualifications',
    templateUrl: './qualifications.component.html',
    styleUrls: ['./qualifications.component.scss']
})
export class QualificationsComponent {

    @Select(FabxState.qualificationsLoadingState) loading$!: Observable<LoadingStateTag>;
    @Select(FabxState.qualifications) qualifications$!: Observable<Qualification[]>;

    constructor(private store: Store) { }
}
