import { Injectable } from "@angular/core";
import { Action, Selector, State, StateContext } from "@ngxs/store";
import { UserService } from "../services/user.service";
import { AugmentedUser, User } from "../models/user.model";
import { Users } from "./user.actions";
import { mergeMap, tap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { getFinishedValueOrDefault, LoadingState, LoadingStateTag } from "./loading-state.model";
import { AuthService } from "../services/auth.service";
import { Auth } from "./auth.actions";
import { Navigate, RouterState, RouterStateModel } from "@ngxs/router-plugin";
import { Qualification } from "../models/qualification.model";
import { Qualifications } from "./qualification.action";
import { QualificationService } from "../services/qualification.service";
import { AugmentedDevice, Device } from "../models/device.model";
import { Devices } from "./device.actions";
import { DeviceService } from "../services/device.service";
import { AugmentedTool, Tool } from "../models/tool.model";
import { Tools } from "./tool.actions";
import { ToolService } from "../services/tool.service";

export interface AuthModel {
    username: string,
    token: string,
}

export interface UserSortModel {
    by: keyof User,
    order: "ascending" | "descending",
}

export interface FabxStateModel {
    auth: AuthModel | null,
    loggedInUserId: string | null,
    users: LoadingState<User[]>,
    usersSort: UserSortModel,
    qualifications: LoadingState<Qualification[]>,
    devices: LoadingState<Device[]>,
    tools: LoadingState<Tool[]>
}

@State<FabxStateModel>({
    name: 'fabx',
    defaults: {
        auth: null,
        loggedInUserId: null,
        users: { tag: "LOADING" },
        usersSort: {
            by: "isAdmin",
            order: "descending",
        },
        qualifications: { tag: "LOADING" },
        devices: { tag: "LOADING" },
        tools: { tag: "LOADING" }
    }
})
@Injectable()
export class FabxState {
    constructor(
        private authService: AuthService,
        private userService: UserService,
        private qualificationService: QualificationService,
        private deviceService: DeviceService,
        private toolService: ToolService,
    ) {}

    // AUTH

    @Selector()
    static auth(state: FabxStateModel): AuthModel | null {
        return state.auth;
    }

    @Selector()
    static isAuthenticated(state: FabxStateModel): boolean {
        return Boolean(state.auth);
    }

    @Action(Auth.Login)
    login(ctx: StateContext<FabxStateModel>, action: Auth.Login) {
        return this.authService.login(action.payload.username, action.payload.password).pipe(
            mergeMap(tokenResponse => {
                ctx.patchState({
                    auth: { username: action.payload.username, token: tokenResponse.token }
                });

                return this.userService.getMe().pipe(tap({
                    next: loggedInUser => {
                        ctx.patchState({
                            loggedInUserId: loggedInUser.id
                        });
                    }
                }));
            })
        );
    }

    @Action(Auth.LoginWebauthn)
    loginWebauthn(ctx: StateContext<FabxStateModel>, action: Auth.LoginWebauthn) {
        return this.authService.loginWebauthn(action.username).pipe(
            mergeMap(tokenResponse => {
                ctx.patchState({
                    auth: { username: action.username, token: tokenResponse.token }
                });

                return this.userService.getMe().pipe(tap({
                    next: loggedInUser => {
                        ctx.patchState({
                            loggedInUserId: loggedInUser.id
                        });
                    }
                }));
            })
        );
    }

    @Action(Auth.Logout)
    logout(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            auth: null,
            loggedInUserId: null
        });
        ctx.dispatch(new Navigate(['login']));
    }

    // USERS

    @Selector()
    static usersLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.users.tag;
    }


    @Selector()
    static users(state: FabxStateModel): AugmentedUser[] {
        const qualifications: Qualification[] = getFinishedValueOrDefault(state.qualifications, []);
        const users: AugmentedUser[] = [...getFinishedValueOrDefault(state.users, [])]
            .map(user => this.augmentUserWithQualifications(user, qualifications));

        const orderMultiplier = state.usersSort.order == "ascending" ? 1 : -1;

        return users.sort((a: AugmentedUser, b: AugmentedUser) => {
            let aVal = a[state.usersSort.by] || false;
            let bVal = b[state.usersSort.by] || false;

            if (typeof aVal === "string") {
                aVal = aVal.toLowerCase();
            }
            if (typeof bVal === "string") {
                bVal = bVal.toLowerCase();
            }

            let ret: number = 0;

            if (aVal < bVal) {
                ret = -1;
            }
            if (aVal > bVal) {
                ret = 1;
            }

            return ret * orderMultiplier;
        });
    }

    @Selector([RouterState])
    static selectedUser(state: FabxStateModel, router: RouterStateModel): AugmentedUser | null {
        const id = router.state?.root.firstChild?.params['id'];

        const qualifications: Qualification[] = getFinishedValueOrDefault(state.qualifications, []);

        if (state.users.tag == "FINISHED" && id) {
            const user = state.users.value.find(user => user.id == id);
            if (user) {
                return this.augmentUserWithQualifications(user, qualifications);
            }
        }
        return null;
    }

    private static augmentUserWithQualifications(user: User, qualifications: Qualification[]): AugmentedUser {
        const memberQualifications = user.memberQualifications
            .map(qualificationId => qualifications.find(qualification => qualification.id == qualificationId))
            .filter((q): q is Qualification => !!q)
            .sort((a: Qualification, b: Qualification) => {
                return a.orderNr - b.orderNr;
            });

        let instructorQualifications: Qualification[] = [];
        if (user.instructorQualifications) {
            instructorQualifications = user.instructorQualifications
                .map(qualificationId => qualifications.find(qualification => qualification.id == qualificationId))
                .filter((q): q is Qualification => !!q)
                .sort((a: Qualification, b: Qualification) => {
                    return a.orderNr - b.orderNr;
                });
        }

        return {
            ...user,
            memberQualifications: memberQualifications,
            instructorQualifications: instructorQualifications
        };
    }

    @Selector([RouterState])
    static availableMemberQualificationsForSelectedUser(state: FabxStateModel, router: RouterStateModel): Qualification[] {
        const userId = router.state?.root.firstChild?.params['id'];

        const qualifications: Qualification[] = getFinishedValueOrDefault(state.qualifications, []);

        const instructorQualificationsOfLoggedInUser = this.loggedInUser(state)?.instructorQualifications;

        if (state.users.tag == "FINISHED" && userId && instructorQualificationsOfLoggedInUser) {
            const user = state.users.value.find(user => user.id == userId);
            if (user) {
                return qualifications
                    .filter(qualification => instructorQualificationsOfLoggedInUser
                        .some(qualificationId => qualification.id === qualificationId))
                    .filter(qualification => !user.memberQualifications
                        .some(qualificationId => qualification.id === qualificationId)
                    );
            }
        }
        return [];
    }

    @Selector([RouterState])
    static availableInstructorQualificationsForSelectedUser(state: FabxStateModel, router: RouterStateModel): Qualification[] {
        const userId = router.state?.root.firstChild?.params['id'];

        const qualifications: Qualification[] = getFinishedValueOrDefault(state.qualifications, []);

        const loggedInUserIsAdmin = this.loggedInUser(state)?.isAdmin || false;

        if (state.users.tag == "FINISHED" && userId && loggedInUserIsAdmin) {
            const user = state.users.value.find(user => user.id == userId);
            if (user) {
                return qualifications.filter(qualification => {
                    if (user.instructorQualifications) {
                        return !user.instructorQualifications.some(qualificationId => qualification.id === qualificationId);
                    } else {
                        return true;
                    }
                });
            }
        }
        return [];
    }

    @Selector()
    static loggedInUser(state: FabxStateModel): User | null {
        if (state.users.tag == "FINISHED" && state.loggedInUserId) {
            return state.users.value.find(user => {
                return user.id == state.loggedInUserId
            }) || null;
        } else {
            return null;
        }
    }

    @Action(Users.GetAll)
    getAllUsers(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            users: { tag: "LOADING" }
        });

        return this.userService.getAllUsers().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        users: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    console.error("error while getting all users: ", err);
                    ctx.patchState({
                        users: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }

    @Action(Users.SetSort)
    setUserSort(ctx: StateContext<FabxStateModel>, action: Users.SetSort) {
        ctx.patchState({
            usersSort: action.sort
        });
    }

    @Action(Users.GetById)
    getUser(ctx: StateContext<FabxStateModel>, action: Users.GetById) {
        return this.userService.getById(action.id).pipe(
            tap({
                next: value => {
                    const state = ctx.getState();
                    if (state.users.tag == "FINISHED") {
                        ctx.patchState({
                            users: {
                                tag: "FINISHED",
                                value: state.users.value.filter((u) => u.id != action.id).concat([value])
                            }
                        });
                    }
                }
            })
        );
    }

    @Action(Users.Add)
    addUser(ctx: StateContext<FabxStateModel>, action: Users.Add) {
        return this.userService.addUser(action.details).pipe(
            tap({
                next: value => {
                    ctx.dispatch(new Users.GetById(value)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['user', value]));
                        }
                    });
                }
            })
        );
    }

    @Action(Users.ChangePersonalInformation)
    changePersonalInformation(ctx: StateContext<FabxStateModel>, action: Users.ChangePersonalInformation) {
        return this.userService.changePersonalInformation(action.userId, action.details).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Users.GetById(action.userId)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['user', action.userId]));
                        }
                    });
                }
            })
        );
    }

    @Action(Users.ChangeLockState)
    changeLockState(ctx: StateContext<FabxStateModel>, action: Users.ChangeLockState) {
        return this.userService.changeLockState(action.userId, action.details).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Users.GetById(action.userId)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['user', action.userId]));
                        }
                    });
                }
            })
        );
    }

    @Action(Users.AddMemberQualification)
    addMemberQualification(ctx: StateContext<FabxStateModel>, action: Users.AddMemberQualification) {
        return this.userService.addMemberQualification(action.userId, action.qualificationId).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.RemoveMemberQualification)
    removeMemberQualification(ctx: StateContext<FabxStateModel>, action: Users.RemoveMemberQualification) {
        return this.userService.removeMemberQualification(action.userId, action.qualificationId).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.AddInstructorQualification)
    addInstructorQualification(ctx: StateContext<FabxStateModel>, action: Users.AddInstructorQualification) {
        return this.userService.addInstructorQualification(action.userId, action.qualificationId).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.RemoveInstructorQualification)
    removeInstructorQualification(ctx: StateContext<FabxStateModel>, action: Users.RemoveInstructorQualification) {
        return this.userService.removeInstructorQualification(action.userId, action.qualificationId).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.ChangeIsAdmin)
    changeIsAdmin(ctx: StateContext<FabxStateModel>, action: Users.ChangeIsAdmin) {
        return this.userService.changeIsAdmin(action.userId, action.isAdmin).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.AddUsernamePasswordIdentity)
    addUsernamePasswordIdentity(ctx: StateContext<FabxStateModel>, action: Users.AddUsernamePasswordIdentity) {
        return this.userService.addUsernamePasswordIdentity(
            action.userId,
            action.details.username,
            action.details.password
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId)).subscribe({
                    next: () => {
                        ctx.dispatch(new Navigate(['user', action.userId]));
                    }
                })
            })
        );
    }

    @Action(Users.RemoveUsernamePasswordIdentity)
    removeUsernamePasswordIdentity(ctx: StateContext<FabxStateModel>, action: Users.RemoveUsernamePasswordIdentity) {
        return this.userService.removeUsernamePasswordIdentity(
            action.userId,
            action.username
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.AddWebauthnIdentity)
    addWebauthnIdentity(ctx: StateContext<FabxStateModel>, action: Users.AddWebauthnIdentity) {
        return this.userService.addWebauthnIdentity(
            action.userId,
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId)).subscribe({
                    next: () => {
                        ctx.dispatch(new Navigate(['user', action.userId]));
                    }
                })
            })
        );
    }

    @Action(Users.RemoveWebauthnIdentity)
    removeWebauthnIdentity(ctx: StateContext<FabxStateModel>, action: Users.RemoveWebauthnIdentity) {
        return this.userService.removeWebauthnIdentity(
            action.userId,
            action.credentialId
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.AddCardIdentity)
    addCardIdentity(ctx: StateContext<FabxStateModel>, action: Users.AddCardIdentity) {
        return this.userService.addCardIdentity(
            action.userId,
            action.details.cardId,
            action.details.cardSecret
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId)).subscribe({
                    next: () => {
                        ctx.dispatch(new Navigate(['user', action.userId]));
                    }
                })
            })
        );
    }

    @Action(Users.RemoveCardIdentity)
    removeCardIdentity(ctx: StateContext<FabxStateModel>, action: Users.RemoveCardIdentity) {
        return this.userService.removeCardIdentity(
            action.userId,
            action.cardId
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.AddPhoneNrIdentity)
    addPhoneNrIdentity(ctx: StateContext<FabxStateModel>, action: Users.AddPhoneNrIdentity) {
        return this.userService.addPhoneNrIdentity(
            action.userId,
            action.details.phoneNr
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId)).subscribe({
                    next: () => {
                        ctx.dispatch(new Navigate(['user', action.userId]));
                    }
                })
            })
        );
    }

    @Action(Users.RemovePhoneNrIdentity)
    removePhoneNrIdentity(ctx: StateContext<FabxStateModel>, action: Users.RemovePhoneNrIdentity) {
        return this.userService.removePhoneNrIdentity(
            action.userId,
            action.phoneNr
        ).pipe(
            tap({
                next: _ => ctx.dispatch(new Users.GetById(action.userId))
            })
        );
    }

    @Action(Users.Delete)
    deleteUser(ctx: StateContext<FabxStateModel>, action: Users.Delete) {
        return this.userService.deleteUser(action.userId).pipe(
            tap({
                next: _ => {
                    const state = ctx.getState();
                    if (state.users.tag == "FINISHED") {
                        ctx.patchState({
                            users: {
                                tag: "FINISHED",
                                value: state.users.value.filter((u) => u.id != action.userId)
                            }
                        });
                    }
                    ctx.dispatch(new Navigate(['user']));
                }
            })
        );
    }

    // QUALIFICATIONS
    @Selector()
    static qualificationsLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.qualifications.tag;
    }

    @Selector()
    static qualifications(state: FabxStateModel): Qualification[] {
        return [...getFinishedValueOrDefault(state.qualifications, [])].sort((a: Qualification, b: Qualification) => {
            return a.orderNr - b.orderNr;
        });
    }

    @Selector([RouterState])
    static selectedQualification(state: FabxStateModel, router: RouterStateModel): Qualification | null {
        const id = router.state?.root.firstChild?.params['id'];

        if (state.qualifications.tag == "FINISHED" && id) {
            const qualification = state.qualifications.value.find(qualification => qualification.id == id);
            return qualification || null;
        }
        return null;
    }

    @Action(Qualifications.GetAll)
    getAllQualifications(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            qualifications: { tag: "LOADING" }
        });

        return this.qualificationService.getAllQualifications().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        qualifications: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    ctx.patchState({
                        qualifications: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }

    @Action(Qualifications.GetById)
    getQualification(ctx: StateContext<FabxStateModel>, action: Qualifications.GetById) {
        return this.qualificationService.getById(action.id).pipe(
            tap({
                next: value => {
                    const state = ctx.getState();
                    if (state.qualifications.tag == "FINISHED") {
                        ctx.patchState({
                            qualifications: {
                                tag: "FINISHED",
                                value: state.qualifications.value.filter((q) => q.id != action.id).concat([value])
                            }
                        });
                    }
                }
            })
        );
    }

    @Action(Qualifications.Add)
    addQualification(ctx: StateContext<FabxStateModel>, action: Qualifications.Add) {
        return this.qualificationService.addQualification(action.details).pipe(
            tap({
                next: value => {
                    ctx.dispatch(new Qualifications.GetById(value)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['qualification', value]));
                        }
                    });
                }
            })
        );
    }

    @Action(Qualifications.ChangeDetails)
    changeQualificationDetails(ctx: StateContext<FabxStateModel>, action: Qualifications.ChangeDetails) {
        return this.qualificationService.changeDetails(action.id, action.details).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Qualifications.GetById(action.id)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['qualification', action.id]));
                        }
                    })
                }
            })
        );
    }

    @Action(Qualifications.Delete)
    deleteQualification(ctx: StateContext<FabxStateModel>, action: Qualifications.Delete) {
        return this.qualificationService.deleteQualification(action.id).pipe(
            tap({
                next: _ => {
                    const state = ctx.getState();
                    if (state.qualifications.tag == "FINISHED") {
                        ctx.patchState({
                            qualifications: {
                                tag: "FINISHED",
                                value: state.qualifications.value.filter((q) => q.id != action.id)
                            }
                        });
                    }
                    ctx.dispatch(new Navigate(['qualification']));
                }
            })
        );
    }

    // DEVICES
    @Selector()
    static devicesLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.devices.tag;
    }

    @Selector()
    static devices(state: FabxStateModel): AugmentedDevice[] {
        const tools: Tool[] = getFinishedValueOrDefault(state.tools, []);
        return getFinishedValueOrDefault(state.devices, [])
            .map(device => this.augmentDeviceWithTools(device, tools));
    }

    @Selector([RouterState])
    static selectedDevice(state: FabxStateModel, router: RouterStateModel): AugmentedDevice | null {
        const id = router.state?.root.firstChild?.params['id'];

        const tools: Tool[] = getFinishedValueOrDefault(state.tools, []);

        if (state.devices.tag == "FINISHED" && id) {
            const device = state.devices.value.find(device => device.id == id);
            if (device) {
                return this.augmentDeviceWithTools(device, tools);
            }
        }
        return null;
    }

    private static augmentDeviceWithTools(device: Device, tools: Tool[]): AugmentedDevice {
        const attachedTools = Array.from(
            Object.entries(device.attachedTools),
            ([pin, toolId]) => [Number(pin), tools.find(tool => tool.id == toolId)!]
        ).filter(([_, v]) => v);

        return {
            ...device,
            attachedTools: Object.fromEntries(attachedTools)
        }
    }

    @Action(Devices.GetAll)
    getAllDevices(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            devices: { tag: "LOADING" }
        });

        return this.deviceService.getAllDevices().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        devices: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    ctx.patchState({
                        devices: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }

    @Action(Devices.GetById)
    getDevice(ctx: StateContext<FabxStateModel>, action: Devices.GetById) {
        return this.deviceService.getById(action.id).pipe(
            tap({
                next: value => {
                    const state = ctx.getState();
                    if (state.devices.tag == "FINISHED") {
                        ctx.patchState({
                            devices: {
                                tag: "FINISHED",
                                value: state.devices.value.filter((d) => d.id != action.id).concat([value])
                            }
                        });
                    }
                }
            })
        );
    }

    @Action(Devices.Add)
    addDevice(ctx: StateContext<FabxStateModel>, action: Devices.Add) {
        return this.deviceService.addDevice(action.details).pipe(
            tap({
                next: value => {
                    ctx.dispatch(new Devices.GetById(value)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['device', value]));
                        }
                    });
                }
            })
        );
    }

    @Action(Devices.ChangeDetails)
    changeDeviceDetails(ctx: StateContext<FabxStateModel>, action: Devices.ChangeDetails) {
        return this.deviceService.changeDetails(action.id, action.details).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Devices.GetById(action.id)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['device', action.id]));
                        }
                    })
                }
            })
        );
    }

    @Action(Devices.AttachTool)
    attachTool(ctx: StateContext<FabxStateModel>, action: Devices.AttachTool) {
        return this.deviceService.attachTool(action.deviceId, action.pin, action.toolId).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Devices.GetById(action.deviceId)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['device', action.deviceId]))
                        }
                    });
                }
            })
        );
    }

    @Action(Devices.DetachTool)
    detachTool(ctx: StateContext<FabxStateModel>, action: Devices.DetachTool) {
        return this.deviceService.detachTool(action.deviceId, action.pin).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Devices.GetById(action.deviceId));
                }
            })
        );
    }

    @Action(Devices.UnlockTool)
    unlockTool(ctx: StateContext<FabxStateModel>, action: Devices.UnlockTool) {
        return this.deviceService.unlockTool(
            action.deviceId,
            {
                toolId: action.toolId
            }
        );
    }

    @Action(Devices.Delete)
    deleteDevice(ctx: StateContext<FabxStateModel>, action: Devices.Delete) {
        return this.deviceService.deleteDevice(action.id).pipe(
            tap({
                next: _ => {
                    const state = ctx.getState();
                    if (state.devices.tag == "FINISHED") {
                        ctx.patchState({
                            devices: {
                                tag: "FINISHED",
                                value: state.devices.value.filter((d) => d.id != action.id)
                            }
                        });
                    }
                    ctx.dispatch(new Navigate(['device']));
                }
            })
        );
    }

    // TOOLS

    @Selector()
    static toolsLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.tools.tag;
    }

    @Selector()
    static tools(state: FabxStateModel): AugmentedTool[] {
        let qualifications: Qualification[] = getFinishedValueOrDefault(state.qualifications, []);
        return getFinishedValueOrDefault(state.tools, [])
            .map(tool => this.augmentToolWithQualifications(tool, qualifications));
    }

    @Selector([RouterState])
    static selectedTool(state: FabxStateModel, router: RouterStateModel): AugmentedTool | null {
        const id = router.state?.root.firstChild?.params['id'];

        const qualifications: Qualification[] = [...getFinishedValueOrDefault(state.qualifications, [])];

        if (state.tools.tag == "FINISHED" && id) {
            const tool = state.tools.value.find(tool => tool.id == id);
            if (tool) {
                return this.augmentToolWithQualifications(tool, qualifications);
            }
        }
        return null;
    }

    @Action(Tools.GetAll)
    getAllTools(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            tools: { tag: "LOADING" }
        });

        return this.toolService.getAllTools().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        tools: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    ctx.patchState({
                        tools: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }

    @Action(Tools.GetById)
    getTool(ctx: StateContext<FabxStateModel>, action: Tools.GetById) {
        return this.toolService.getById(action.id).pipe(
            tap({
                next: value => {
                    const state = ctx.getState();
                    if (state.tools.tag == "FINISHED") {
                        ctx.patchState({
                            tools: {
                                tag: "FINISHED",
                                value: state.tools.value.filter((t) => t.id != action.id).concat([value])
                            }
                        });
                    }
                }
            })
        );
    }

    @Action(Tools.Add)
    addTool(ctx: StateContext<FabxStateModel>, action: Tools.Add) {
        return this.toolService.addTool(action.details).pipe(
            tap({
                next: value => {
                    ctx.dispatch(new Tools.GetById(value)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['tool', value]));
                        }
                    });
                }
            })
        );
    }

    @Action(Tools.ChangeDetails)
    changeToolDetails(ctx: StateContext<FabxStateModel>, action: Tools.ChangeDetails) {
        return this.toolService.changeDetails(action.id, action.details).pipe(
            tap({
                next: _ => {
                    ctx.dispatch(new Tools.GetById(action.id)).subscribe({
                        next: () => {
                            ctx.dispatch(new Navigate(['tool', action.id]));
                        }
                    })
                }
            })
        );
    }

    @Action(Tools.Delete)
    deleteTool(ctx: StateContext<FabxStateModel>, action: Tools.Delete) {
        return this.toolService.deleteTool(action.id).pipe(
            tap({
                next: _ => {
                    const state = ctx.getState();
                    if (state.tools.tag == "FINISHED") {
                        ctx.patchState({
                            tools: {
                                tag: "FINISHED",
                                value: state.tools.value.filter((t) => t.id != action.id)
                            }
                        });
                    }
                    ctx.dispatch(new Navigate(['tool']));
                }
            })
        );
    }

    private static augmentToolWithQualifications(tool: Tool, qualifications: Qualification[]): AugmentedTool {
        const requiredQualifications = tool.requiredQualifications
            .map(qualificationId => qualifications.find(qualification => qualification.id == qualificationId))
            .filter((q): q is Qualification => !!q)
            .sort((a: Qualification, b: Qualification) => {
                return a.orderNr - b.orderNr;
            });

        return {
            ...tool,
            requiredQualifications: requiredQualifications
        };
    }
}
