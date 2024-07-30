import { Component, Input } from '@angular/core';

@Component({
    selector: 'fabx-hidden-pin',
    templateUrl: './hidden-pin.component.html',
    styleUrls: ['./hidden-pin.component.scss']
})
export class HiddenPinComponent {
    @Input() pin: string = "";

    hidden: boolean = true;

    getHiddenPin(): string {
        return "*".repeat(this.pin.length);
    }
}
