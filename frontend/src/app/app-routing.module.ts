import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UsersComponent } from "./users/users.component";
import { LoginComponent } from "./login/login.component";
import { AuthGuard } from "./state/auth-guard";

const routes: Routes = [
    { path: '', redirectTo: '/user', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'user', component: UsersComponent, canActivate: [AuthGuard] },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
