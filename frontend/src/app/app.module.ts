import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NgxsModule } from "@ngxs/store";
import { NgxsStoragePluginModule } from "@ngxs/storage-plugin";
import { NgxsReduxDevtoolsPluginModule } from "@ngxs/devtools-plugin";

import { ButtonModule } from 'primeng/button';
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ColorPickerModule } from "primeng/colorpicker";
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from "primeng/inputtext";
import { MessageModule } from 'primeng/message';
import { MessagesModule } from "primeng/messages";
import { MultiSelectModule } from "primeng/multiselect";
import { PasswordModule } from "primeng/password";
import { SkeletonModule } from "primeng/skeleton";
import { SplitButtonModule } from "primeng/splitbutton";
import { StyleClassModule } from "primeng/styleclass";
import { TableModule } from "primeng/table";
import { TagModule } from "primeng/tag";

import { environment } from "../environments/environment";
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AttachedToolNames, DevicesComponent } from './devices/devices.component';
import { AuthGuard } from "./state/auth-guard";
import { DeviceAttachToolComponent } from './device-attach-tool/device-attach-tool.component';
import { DeviceChangeDetailsComponent } from './device-change-details/device-change-details.component';
import { DeviceDetailsComponent } from './device-details/device-details.component';
import { FabxState } from "./state/fabx-state";
import { LoginComponent } from './login/login.component';
import { NavbarComponent } from './navbar/navbar.component';
import { NgxsRouterPluginModule } from "@ngxs/router-plugin";
import { QualificationAddComponent } from './qualification-add/qualification-add.component';
import { QualificationChangeDetailsComponent } from './qualification-change-details/qualification-change-details.component';
import { QualificationDetailsComponent } from './qualification-details/qualification-details.component';
import { QualificationsComponent } from './qualifications/qualifications.component';
import { ToolAddComponent } from './tool-add/tool-add.component';
import { ToolChangeDetailsComponent } from './tool-change-details/tool-change-details.component';
import { ToolDetailsComponent } from './tool-details/tool-details.component';
import { ToolsComponent } from './tools/tools.component';
import { UserAddComponent } from './user-add/user-add.component';
import { UserAddUsernamePasswordIdentityComponent } from './user-add-username-password-identity/user-add-username-password-identity.component';
import { UserChangeLockStateComponent } from './user-change-lock-state/user-change-lock-state.component';
import { UserChangePersonalInfoComponent } from './user-change-personal-info/user-change-personal-info.component';
import { UserDetailsComponent } from './user-details/user-details.component';
import { UsersComponent } from './users/users.component';

@NgModule({
    declarations: [
        AppComponent,
        AttachedToolNames,
        DeviceAttachToolComponent,
        DeviceChangeDetailsComponent,
        DeviceDetailsComponent,
        DevicesComponent,
        LoginComponent,
        NavbarComponent,
        QualificationAddComponent,
        QualificationChangeDetailsComponent,
        QualificationDetailsComponent,
        QualificationsComponent,
        ToolAddComponent,
        ToolChangeDetailsComponent,
        ToolDetailsComponent,
        ToolsComponent,
        UserAddComponent,
        UserAddUsernamePasswordIdentityComponent,
        UserChangeLockStateComponent,
        UserChangePersonalInfoComponent,
        UserDetailsComponent,
        UsersComponent,
    ],
    imports: [
        BrowserAnimationsModule,
        BrowserModule,
        HttpClientModule,
        ReactiveFormsModule,
        FormsModule,

        NgxsModule.forRoot([FabxState], {
            developmentMode: !environment.production
        }),
        NgxsStoragePluginModule.forRoot({
            key: ["fabx.auth", "fabx.loggedInUserId"]
        }),
        NgxsRouterPluginModule.forRoot(),
        NgxsReduxDevtoolsPluginModule.forRoot(),

        ButtonModule,
        CardModule,
        CheckboxModule,
        ColorPickerModule,
        ConfirmDialogModule,
        DropdownModule,
        InputNumberModule,
        InputTextModule,
        MessageModule,
        MessagesModule,
        MultiSelectModule,
        PasswordModule,
        SkeletonModule,
        SplitButtonModule,
        StyleClassModule,
        TableModule,
        TagModule,

        AppRoutingModule,
    ],
    providers: [AuthGuard],
    bootstrap: [AppComponent]
})
export class AppModule {
}
