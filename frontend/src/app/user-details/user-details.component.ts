import { Component } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable } from "rxjs";
import { UserVM } from "../models/user.model";

@Component({
    selector: 'fabx-user-details',
    templateUrl: './user-details.component.html',
    styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent {

    @Select(FabxState.selectedUser) user$!: Observable<UserVM>;

    constructor(private store: Store) { }
}
