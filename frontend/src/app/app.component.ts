import { Component, Inject, OnInit } from '@angular/core';
import { DOCUMENT } from "@angular/common";

@Component({
    selector: 'fabx-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    title = 'fabX';

    constructor(@Inject(DOCUMENT) private document: Document) {}

    ngOnInit(): void {
        let themeLink = this.document.getElementById('app-theme') as HTMLLinkElement;

        const darkModeOn = window.matchMedia("(prefers-color-scheme: dark)").matches;

        if (darkModeOn) {
            themeLink.href = 'theme-dark.css';
        }

        window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", e => {
            const darkModeOn = e.matches;

            if (darkModeOn) {
                themeLink.href = 'theme-dark.css';
            } else {
                themeLink.href = 'theme-light.css';
            }
        });
    }
}
