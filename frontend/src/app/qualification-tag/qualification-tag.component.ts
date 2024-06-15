import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Qualification } from "../models/qualification.model";

@Component({
    selector: 'fabx-qualification-tag',
    templateUrl: './qualification-tag.component.html',
    styleUrls: ['./qualification-tag.component.scss']
})
export class QualificationTagComponent {
    @Input() qualification: Qualification | undefined;
    @Input() showRemoveIcon: boolean = false;
    @Output() removeEventEmitter: EventEmitter<any> = new EventEmitter();

    removeClicked() {
        this.removeEventEmitter.emit();
    }
}
