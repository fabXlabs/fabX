import { Component, OnInit } from '@angular/core';
import { User } from "../models/user.model";
import { UserService } from "../services/user.service";

@Component({
    selector: 'fabx-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

    users: User[] = []

    constructor(private userService: UserService) {}

    ngOnInit(): void {
        this.userService.getAllUsers().subscribe({
            next: (val) => { this.users = val },
            error: (err) => { console.error("error while getting all users: %o", err) }
        })
    }
}
