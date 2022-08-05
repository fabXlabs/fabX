import { Component, OnDestroy } from '@angular/core';
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { Observable, Subscription } from "rxjs";
import { UserVM } from "../models/user.model";
import { Qualification } from "../models/qualification.model";
import { MenuItem } from "primeng/api";
import { Users } from "../state/user.actions";

@Component({
    selector: 'fabx-user-details',
    templateUrl: './user-details.component.html',
    styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent implements OnDestroy {

    @Select(FabxState.selectedUser) user$!: Observable<UserVM>;
    @Select(FabxState.availableMemberQualificationsForSelectedUser) missingMemberQualifications$!: Observable<Qualification[]>;
    private subscription: Subscription;

    items: MenuItem[] = [];

    constructor(private store: Store) {
        this.subscription = this.missingMemberQualifications$.subscribe({
            next: value => {
                this.items = value.map(qualification => {
                    return {
                        label: qualification.name,
                        command: _ => this.addMemberQualification(qualification.id)
                    };
                });
            }
        });
    }

    addMemberQualification(qualificationId: string) {
        this.store.dispatch(new Users.AddMemberQualification(
            this.store.selectSnapshot(FabxState.selectedUser)!.id,
            qualificationId
        ));
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
