import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { authGuard } from "./state/auth-guard";
import { DeviceAddComponent } from "./device-add/device-add.component";
import { DeviceAttachToolComponent } from "./device-attach-tool/device-attach-tool.component";
import { DeviceChangeDesiredFirmwareVersionComponent } from "./device-change-desired-firmware-version/device-change-desired-firmware-version.component";
import { DeviceChangeDetailsComponent } from "./device-change-details/device-change-details.component";
import { DeviceChangeThumbnailComponent } from "./device-change-thumbnail/device-change-thumbnail.component";
import { DeviceDetailsComponent } from "./device-details/device-details.component";
import { DevicesComponent } from "./devices/devices.component";
import { LoginComponent } from "./login/login.component";
import { QualificationAddComponent } from "./qualification-add/qualification-add.component";
import { QualificationChangeDetailsComponent } from "./qualification-change-details/qualification-change-details.component";
import { QualificationDetailsComponent } from "./qualification-details/qualification-details.component";
import { QualificationsComponent } from "./qualifications/qualifications.component";
import { ToolAddComponent } from "./tool-add/tool-add.component";
import { ToolChangeDetailsComponent } from "./tool-change-details/tool-change-details.component";
import { ToolChangeThumbnailComponent } from "./tool-change-thumbnail/tool-change-thumbnail.component";
import { ToolDetailsComponent } from "./tool-details/tool-details.component";
import { ToolsComponent } from "./tools/tools.component";
import { UserAddCardIdentityComponent } from "./user-add-card-identity/user-add-card-identity.component";
import { UserAddComponent } from "./user-add/user-add.component";
import { UserAddPhoneNrIdentityComponent } from "./user-add-phone-nr-identity/user-add-phone-nr-identity.component";
import { UserAddPinIdentityComponent } from "./user-add-pin-identity/user-add-pin-identity.component";
import { UserAddQualificationComponent } from "./user-add-qualification/user-add-qualification.component";
import { UserAddUsernamePasswordIdentityComponent } from "./user-add-username-password-identity/user-add-username-password-identity.component";
import { UserAddWebauthnIdentityComponent } from "./user-add-webauthn-identity/user-add-webauthn-identity.component";
import { UserChangeLockStateComponent } from "./user-change-lock-state/user-change-lock-state.component";
import { UserChangePasswordComponent } from "./user-change-password/user-change-password.component";
import { UserChangePersonalInfoComponent } from "./user-change-personal-info/user-change-personal-info.component";
import { UserDetailsComponent } from "./user-details/user-details.component";
import { UserSoftDeletedComponent } from "./user-soft-deleted/user-soft-deleted.component";
import { UsersComponent } from "./users/users.component";

const routes: Routes = [
    { path: '', redirectTo: '/user', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'user', component: UsersComponent, canActivate: [authGuard] },
    { path: 'user/add', component: UserAddComponent, canActivate: [authGuard] },
    { path: 'user/soft-deleted', component: UserSoftDeletedComponent, canActivate: [authGuard] },
    { path: 'user/:id', component: UserDetailsComponent, canActivate: [authGuard] },
    { path: 'user/:id/add-card-identity', component: UserAddCardIdentityComponent, canActivate: [authGuard] },
    { path: 'user/:id/add-phone-nr-identity', component: UserAddPhoneNrIdentityComponent, canActivate: [authGuard] },
    { path: 'user/:id/add-username-password-identity', component: UserAddUsernamePasswordIdentityComponent, canActivate: [authGuard] },
    { path: 'user/:id/add-pin-identity', component: UserAddPinIdentityComponent, canActivate: [authGuard] },
    { path: 'user/:id/change-password', component: UserChangePasswordComponent, canActivate: [authGuard] },
    { path: 'user/:id/add-webauthn-identity', component: UserAddWebauthnIdentityComponent, canActivate: [authGuard] },
    { path: 'user/:id/change-personal-info', component: UserChangePersonalInfoComponent, canActivate: [authGuard] },
    { path: 'user/:id/lock', component: UserChangeLockStateComponent, canActivate: [authGuard] },
    { path: 'qualification', component: QualificationsComponent, canActivate: [authGuard] },
    { path: 'qualification/add', component: QualificationAddComponent, canActivate: [authGuard] },
    { path: 'qualification/:id', component: QualificationDetailsComponent, canActivate: [authGuard] },
    { path: 'qualification/:id/change-details', component: QualificationChangeDetailsComponent, canActivate: [authGuard] },
    { path: 'device', component: DevicesComponent, canActivate: [authGuard] },
    { path: 'device/add', component: DeviceAddComponent, canActivate: [authGuard] },
    { path: 'device/:id', component: DeviceDetailsComponent, canActivate: [authGuard] },
    { path: 'device/:id/change-details', component: DeviceChangeDetailsComponent, canActivate: [authGuard] },
    { path: 'device/:id/change-thumbnail', component: DeviceChangeThumbnailComponent, canActivate: [authGuard] },
    { path: 'device/:id/change-desired-firmware-version', component: DeviceChangeDesiredFirmwareVersionComponent, canActivate: [authGuard] },
    { path: 'device/:id/attach-tool', component: DeviceAttachToolComponent, canActivate: [authGuard] },
    { path: 'tool', component: ToolsComponent, canActivate: [authGuard] },
    { path: 'tool/add', component: ToolAddComponent, canActivate: [authGuard] },
    { path: 'tool/:id', component: ToolDetailsComponent, canActivate: [authGuard] },
    { path: 'tool/:id/change-details', component: ToolChangeDetailsComponent, canActivate: [authGuard] },
    { path: 'tool/:id/change-thumbnail', component: ToolChangeThumbnailComponent, canActivate: [authGuard] },
    { path: 'add-qualification', component: UserAddQualificationComponent, canActivate: [authGuard] },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
