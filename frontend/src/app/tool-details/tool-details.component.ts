import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { AugmentedTool } from "../models/tool.model";

@Component({
    selector: 'fabx-tool-details',
    templateUrl: './tool-details.component.html',
    styleUrls: ['./tool-details.component.scss']
})
export class ToolDetailsComponent {

    @Select(FabxState.selectedTool) tool$!: Observable<AugmentedTool>;

    constructor(private store: Store) { }
}
