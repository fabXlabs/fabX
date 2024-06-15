import { Component } from '@angular/core';
import { Store } from "@ngxs/store";
import { ErrorService } from "../services/error.service";
import { Tools } from "../state/tool.actions";
import { FabxState } from "../state/fabx-state";
import { HttpErrorResponse } from "@angular/common/http";
import { FormControl, FormGroup } from "@angular/forms";

@Component({
    selector: 'fabx-tool-change-thumbnail',
    templateUrl: './tool-change-thumbnail.component.html',
    styleUrls: ['./tool-change-thumbnail.component.scss']
})
export class ToolChangeThumbnailComponent {

    error = "";

    form = new FormGroup({
        file: new FormControl('')
    });

    constructor(private store: Store, private errorHandler: ErrorService) { }

    onFileSelected(event: Event) {
        const target = event.target as HTMLInputElement;
        const files = target.files as FileList;
        const file = files[0];

        const currentTool = this.store.selectSnapshot(FabxState.selectedTool)!;

        if (file) {
            console.debug("onFileSelected: {}", file)

            this.store.dispatch(new Tools.ChangeThumbnail(currentTool.id, file))
                .subscribe({
                    error: (err: HttpErrorResponse) => {
                        this.error = this.errorHandler.format(err);
                    }
                });
        }
    }
}
