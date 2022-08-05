import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UsersComponent } from "./users/users.component";
import { LoginComponent } from "./login/login.component";
import { AuthGuard } from "./state/auth-guard";
import { UserDetailsComponent } from "./user-details/user-details.component";
import { UserAddComponent } from "./user-add/user-add.component";

const routes: Routes = [
    { path: '', redirectTo: '/user', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'user', component: UsersComponent, canActivate: [AuthGuard] },
    { path: 'user/add', component: UserAddComponent, canActivate: [AuthGuard] },
    { path: 'user/:id', component: UserDetailsComponent, canActivate: [AuthGuard] },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
