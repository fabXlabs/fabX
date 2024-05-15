import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
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
import { ToastModule } from 'primeng/toast';

import { environment } from "../environments/environment";
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AttachedToolNames, DevicesComponent } from './devices/devices.component';
import { AuthGuard } from "./state/auth-guard";
import { AuthInterceptor } from "./state/auth-interceptor";
import { DeviceAddComponent } from './device-add/device-add.component';
import { DeviceAttachToolComponent } from './device-attach-tool/device-attach-tool.component';
import { DeviceChangeDetailsComponent } from './device-change-details/device-change-details.component';
import { DeviceDetailsComponent } from './device-details/device-details.component';
import { DeviceChangeDesiredFirmwareVersionComponent } from './device-change-desired-firmware-version/device-change-desired-firmware-version.component';
import { FabxState } from "./state/fabx-state";
import { LoginComponent } from './login/login.component';
import { LogoutOnUnauthorizedInterceptorService } from "./services/logout-on-unauthorized-interceptor.service";
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
import { UserAddCardIdentityComponent } from './user-add-card-identity/user-add-card-identity.component';
import { UserAddComponent } from './user-add/user-add.component';
import { UserAddPhoneNrIdentityComponent } from './user-add-phone-nr-identity/user-add-phone-nr-identity.component';
import { UserAddPinIdentityComponent } from './user-add-pin-identity/user-add-pin-identity.component';
import { UserAddQualificationComponent } from './user-add-qualification/user-add-qualification.component';
import { UserAddUsernamePasswordIdentityComponent } from './user-add-username-password-identity/user-add-username-password-identity.component';
import { UserAddWebauthnIdentityComponent } from './user-add-webauthn-identity/user-add-webauthn-identity.component';
import { UserChangeLockStateComponent } from './user-change-lock-state/user-change-lock-state.component';
import { UserChangePasswordComponent } from "./user-change-password/user-change-password.component";
import { UserChangePersonalInfoComponent } from './user-change-personal-info/user-change-personal-info.component';
import { UserDetailsComponent } from './user-details/user-details.component';
import { UserSoftDeletedComponent } from './user-soft-deleted/user-soft-deleted.component';
import { UsersComponent } from './users/users.component';
import { ZXingScannerModule } from "@zxing/ngx-scanner";

@NgModule({
    declarations: [
        AppComponent,
        AttachedToolNames,
        DeviceAddComponent,
        DeviceAttachToolComponent,
        DeviceChangeDetailsComponent,
        DeviceDetailsComponent,
        DeviceChangeDesiredFirmwareVersionComponent,
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
        UserAddCardIdentityComponent,
        UserAddComponent,
        UserAddPhoneNrIdentityComponent,
        UserAddPinIdentityComponent,
        UserAddQualificationComponent,
        UserAddUsernamePasswordIdentityComponent,
        UserAddWebauthnIdentityComponent,
        UserChangeLockStateComponent,
        UserChangePasswordComponent,
        UserChangePersonalInfoComponent,
        UserDetailsComponent,
        UserSoftDeletedComponent,
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
            key: ["fabx.auth", "fabx.loggedInUserId", "fabx.lastAuthenticatedUsername"]
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
        ToastModule,

        ZXingScannerModule,

        AppRoutingModule
    ],
    providers: [
        AuthGuard,
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: LogoutOnUnauthorizedInterceptorService, multi: true }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
