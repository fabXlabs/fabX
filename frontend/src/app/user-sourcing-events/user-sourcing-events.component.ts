import { Component, OnDestroy, OnInit } from "@angular/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { UserService } from "../services/user.service";
import { Select, Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { combineLatest, filter, mergeMap, Observable, Subscription } from "rxjs";
import {
    AugmentedUser,
    AugmentedUserSourcingEvent,
    DeviceActorId,
    SystemActorId,
    UserActorId,
    UserSourcingEvent
} from "../models/user.model";
import { formatDate } from "@angular/common";
import { Qualification } from "../models/qualification.model";

@Component({
    selector: 'fabx-user-sourcing-events',
    templateUrl: './user-sourcing-events.component.html',
    styleUrls: ['./user-sourcing-events.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class UserSourcingEventsComponent implements OnInit, OnDestroy {

    @Select(FabxState.selectedUser) user$!: Observable<AugmentedUser>;
    @Select(FabxState.allUsers) users$!: Observable<AugmentedUser[]>;
    @Select(FabxState.qualifications) qualifications$!: Observable<Qualification[]>;

    sourcingEvents$!: Observable<AugmentedUserSourcingEvent[]>;
    private sourcingEventSubscription: Subscription | null = null;

    onlyShowFirst = true;

    constructor(
        private store: Store,
        public userService: UserService
    ) {}

    ngOnInit(): void {
        let sourcingEvents$ = this.user$.pipe(
            filter(u => u != null),
            mergeMap(user => {
                return this.userService.getSourcingEventsById(user.id);
            }),
            mergeMap(sourcingEvents =>
                [sourcingEvents.reverse()]
            )
        );

        this.sourcingEvents$ = combineLatest([sourcingEvents$, this.users$, this.qualifications$]).pipe(
            mergeMap(e => {
                const [userSourcingEvents, augmentedUsers, qualifications] = e;

                return [
                    userSourcingEvents
                        .map(event => {
                            let augmentedActor: SystemActorId | DeviceActorId | UserActorId | AugmentedUser;
                            if (event.actorId.type == "cloud.fabX.fabXaccess.common.model.UserId") {
                                const actorUserId = event.actorId.value;
                                augmentedActor = augmentedUsers.find(u => u.id == actorUserId) || event.actorId;
                            } else {
                                augmentedActor = event.actorId;
                            }

                            return { ...event, actor: augmentedActor, actorId: undefined };
                        })
                        .map(event => {
                            if ("qualificationId" in event) {
                                const qualificationId = event.qualificationId as {value: string}
                                const qualification = qualifications.find(q => q.id == qualificationId.value) || null;
                                return { ...event, qualification: qualification, qualificationId: undefined};
                            } else {
                                return event;
                            }
                        })
                ];
            })
        )

        this.sourcingEventSubscription = this.sourcingEvents$.subscribe({
            next: value => {
                console.log("sourcing events", value);
            },
            error: err => {
                console.error("sourcing events", err);
            }
        });
    }

    ngOnDestroy(): void {
        this.sourcingEventSubscription?.unsubscribe();
    }

    getSourcingFirstOrAllEvents(events: UserSourcingEventEntry[] , onlyFirst: boolean): UserSourcingEventEntry[] {
        if (onlyFirst && events.length > 4) {
            const marker: ShowAllEntriesMarker[] = [{marker: "ShowAllEntriesMarker"}]
            return events.slice(0, 3).concat(marker);
        } else {
            return events;
        }
    }

    extractType(userSourcingEvent: AugmentedUserSourcingEvent): string {
        let type = userSourcingEvent.type.split(".").pop() || "";
        return type.replace(/([A-Z])/g, " $1");
    }

    extractTimestamp(userSourcingEvents: AugmentedUserSourcingEvent): string {
        let ts = Date.parse(userSourcingEvents.timestamp);
        return formatDate(ts, "yyyy-MM-dd hh:mm", "en_US");
    }

    extractActor(userSourcingEvent: AugmentedUserSourcingEvent): string {
        if ('type' in userSourcingEvent.actor) {
            switch (userSourcingEvent.actor.type) {
                case "cloud.fabX.fabXaccess.common.model.SystemActorId":
                    return "SYSTEM"
                case "cloud.fabX.fabXaccess.common.model.UserId":
                    return `User ${userSourcingEvent.actor.value}`
                case "cloud.fabX.fabXaccess.common.model.DeviceId":
                    return `Device ${userSourcingEvent.actor.value}`
            }
        } else {
            const au = userSourcingEvent.actor as AugmentedUser;
            return `${au.firstName} ${au.lastName} (${au.wikiName})`;
        }
    }

    extractQualification(userSourcingEvent: AugmentedUserSourcingEvent): Qualification | null {
        if ("qualification" in userSourcingEvent) {
            return userSourcingEvent.qualification as Qualification;
        } else {
            return null;
        }
    }

    extractPin(userSourcingEvent: AugmentedUserSourcingEvent): string | null {
        if ("pin" in userSourcingEvent) {
            return userSourcingEvent.pin as string;
        } else {
            return null;
        }
    }

    private newValueToString(value: object): string {
        try {
            if ("type" in value) {
                if (value.type == "cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs") {
                    return "";
                } else if ((value.type as string).startsWith("cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue") && "value" in value) {
                    return value.value as string;
                }
            }
        } catch (e) { }
        return JSON.stringify(value);
    }

    extractOther(userSourcingEvent: UserSourcingEvent): [string, string][] {
        const reducedEvent = {
            ...userSourcingEvent,
            type: undefined,
            aggregateRootId: undefined,
            aggregateVersion: undefined,
            actor: undefined,
            correlationId: undefined,
            timestamp: undefined,
            authenticator: undefined,
            hash: undefined,
            pin: undefined,
            cardSecret: undefined
        }

        return Object.entries(reducedEvent)
            .filter(entry => entry[1])
            .map(entry => [entry[0], this.newValueToString(entry[1] as object)])
            .filter(entry => entry[1]) as [string, string][]
    }

    isMarker(entry: UserSourcingEventEntry) {
        return "marker" in entry && entry.marker == "ShowAllEntriesMarker";
    }
}

export interface ShowAllEntriesMarker {
    marker: "ShowAllEntriesMarker"
}
export type UserSourcingEventEntry = AugmentedUserSourcingEvent | ShowAllEntriesMarker;
