import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from "./state/auth-guard";
import { LoginComponent } from "./login/login.component";
import { QualificationAddComponent } from "./qualification-add/qualification-add.component";
import { QualificationsComponent } from "./qualifications/qualifications.component";
import { UserAddComponent } from "./user-add/user-add.component";
import { UserDetailsComponent } from "./user-details/user-details.component";
import { UsersComponent } from "./users/users.component";
import { QualificationDetailsComponent } from "./qualification-details/qualification-details.component";

const routes: Routes = [
    { path: '', redirectTo: '/user', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'user', component: UsersComponent, canActivate: [AuthGuard] },
    { path: 'user/add', component: UserAddComponent, canActivate: [AuthGuard] },
    { path: 'user/:id', component: UserDetailsComponent, canActivate: [AuthGuard] },
    { path: 'qualification', component: QualificationsComponent, canActivate: [AuthGuard] },
    { path: 'qualification/add', component: QualificationAddComponent, canActivate: [AuthGuard] },
    { path: 'qualification/:id', component: QualificationDetailsComponent, canActivate: [AuthGuard] },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
