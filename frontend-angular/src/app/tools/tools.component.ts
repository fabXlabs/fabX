import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { LoadingStateTag } from "../state/loading-state.model";
import { AugmentedTool } from "../models/tool.model";
import { User } from "../models/user.model";
import { ToolService } from "../services/tool.service";

@Component({
    selector: 'fabx-tools',
    templateUrl: './tools.component.html',
    styleUrls: ['./tools.component.scss']
})
export class ToolsComponent {

    @Select(FabxState.toolsLoadingState) loading$!: Observable<LoadingStateTag>;
    @Select(FabxState.tools) tools$!: Observable<AugmentedTool[]>;
    @Select(FabxState.loggedInUser) loggedInUser$!: Observable<User>;

    constructor(private store: Store, private toolService: ToolService) { }

    thumbnailUrl(id: string) {
        return this.toolService.thumbnailUrl(id);
    }
}
