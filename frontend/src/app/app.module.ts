import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from "@angular/forms";
import { NgxsModule } from "@ngxs/store";

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from "primeng/inputtext";
import { MessageModule } from 'primeng/message';
import { MessagesModule } from "primeng/messages";
import { PasswordModule } from "primeng/password";
import { SkeletonModule } from "primeng/skeleton";
import { TableModule } from "primeng/table";
import { StyleClassModule } from "primeng/styleclass";

import { environment } from "../environments/environment";
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UsersComponent } from './users/users.component';
import { LoginComponent } from './login/login.component';

@NgModule({
    declarations: [
        AppComponent,
        UsersComponent,
        LoginComponent
    ],
    imports: [
        BrowserAnimationsModule,
        BrowserModule,
        HttpClientModule,
        ReactiveFormsModule,

        NgxsModule.forRoot([], {
            developmentMode: !environment.production
        }),

        ButtonModule,
        InputTextModule,
        MessageModule,
        MessagesModule,
        PasswordModule,
        SkeletonModule,
        TableModule,
        StyleClassModule,

        AppRoutingModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
