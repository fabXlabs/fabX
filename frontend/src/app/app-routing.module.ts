import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from "./state/auth-guard";
import { DeviceDetailsComponent } from "./device-details/device-details.component";
import { DevicesComponent } from "./devices/devices.component";
import { LoginComponent } from "./login/login.component";
import { QualificationAddComponent } from "./qualification-add/qualification-add.component";
import { QualificationDetailsComponent } from "./qualification-details/qualification-details.component";
import { QualificationsComponent } from "./qualifications/qualifications.component";
import { ToolAddComponent } from "./tool-add/tool-add.component";
import { ToolDetailsComponent } from "./tool-details/tool-details.component";
import { ToolsComponent } from "./tools/tools.component";
import { UserAddComponent } from "./user-add/user-add.component";
import { UserChangeLockStateComponent } from "./user-change-lock-state/user-change-lock-state.component";
import { UserChangePersonalInfoComponent } from "./user-change-personal-info/user-change-personal-info.component";
import { UserDetailsComponent } from "./user-details/user-details.component";
import { UsersComponent } from "./users/users.component";
import { DeviceAttachToolComponent } from "./device-attach-tool/device-attach-tool.component";

const routes: Routes = [
    { path: '', redirectTo: '/user', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'user', component: UsersComponent, canActivate: [AuthGuard] },
    { path: 'user/add', component: UserAddComponent, canActivate: [AuthGuard] },
    { path: 'user/:id', component: UserDetailsComponent, canActivate: [AuthGuard] },
    { path: 'user/:id/change-personal-info', component: UserChangePersonalInfoComponent, canActivate: [AuthGuard] },
    { path: 'user/:id/lock', component: UserChangeLockStateComponent, canActivate: [AuthGuard] },
    { path: 'qualification', component: QualificationsComponent, canActivate: [AuthGuard] },
    { path: 'qualification/add', component: QualificationAddComponent, canActivate: [AuthGuard] },
    { path: 'qualification/:id', component: QualificationDetailsComponent, canActivate: [AuthGuard] },
    { path: 'device', component: DevicesComponent, canActivate: [AuthGuard] },
    { path: 'device/:id', component: DeviceDetailsComponent, canActivate: [AuthGuard] },
    { path: 'device/:id/attach-tool', component: DeviceAttachToolComponent, canActivate: [AuthGuard] },
    { path: 'tool', component: ToolsComponent, canActivate: [AuthGuard] },
    { path: 'tool/add', component: ToolAddComponent, canActivate: [AuthGuard] },
    { path: 'tool/:id', component: ToolDetailsComponent, canActivate: [AuthGuard] },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
