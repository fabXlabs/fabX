import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from "@angular/forms";
import { NgxsModule } from "@ngxs/store";
import { NgxsStoragePluginModule } from "@ngxs/storage-plugin";
import { NgxsReduxDevtoolsPluginModule } from "@ngxs/devtools-plugin";

import { ButtonModule } from 'primeng/button';
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { InputTextModule } from "primeng/inputtext";
import { MessageModule } from 'primeng/message';
import { MessagesModule } from "primeng/messages";
import { PasswordModule } from "primeng/password";
import { SkeletonModule } from "primeng/skeleton";
import { StyleClassModule } from "primeng/styleclass";
import { TableModule } from "primeng/table";
import { TagModule } from "primeng/tag";

import { environment } from "../environments/environment";
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UsersComponent } from './users/users.component';
import { LoginComponent } from './login/login.component';
import { FabxState } from "./state/fabx-state";
import { AuthGuard } from "./state/auth-guard";
import { NavbarComponent } from './navbar/navbar.component';
import { UserDetailsComponent } from './user-details/user-details.component';
import { NgxsRouterPluginModule } from "@ngxs/router-plugin";

@NgModule({
    declarations: [
        AppComponent,
        UsersComponent,
        LoginComponent,
        NavbarComponent,
        UserDetailsComponent
    ],
    imports: [
        BrowserAnimationsModule,
        BrowserModule,
        HttpClientModule,
        ReactiveFormsModule,

        NgxsModule.forRoot([FabxState], {
            developmentMode: !environment.production
        }),
        NgxsStoragePluginModule.forRoot({
            key: ["fabx.auth", "fabx.loggedInUserId"]
        }),
        NgxsRouterPluginModule.forRoot(),
        NgxsReduxDevtoolsPluginModule.forRoot(),

        ButtonModule,
        InputTextModule,
        MessageModule,
        MessagesModule,
        PasswordModule,
        SkeletonModule,
        TableModule,
        StyleClassModule,

        AppRoutingModule,
        CardModule,
        TagModule,
        CheckboxModule
    ],
    providers: [AuthGuard],
    bootstrap: [AppComponent]
})
export class AppModule {
}
