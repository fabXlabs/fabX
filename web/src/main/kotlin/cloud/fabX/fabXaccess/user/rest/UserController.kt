package cloud.fabX.fabXaccess.user.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readHexStringParameter
import cloud.fabX.fabXaccess.common.rest.readInstructorAuthentication
import cloud.fabX.fabXaccess.common.rest.readMemberAuthentication
import cloud.fabX.fabXaccess.common.rest.readStringParameter
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.requireStringQueryParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toByteArray
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.user.application.AddingCardIdentity
import cloud.fabX.fabXaccess.user.application.AddingInstructorQualification
import cloud.fabX.fabXaccess.user.application.AddingMemberQualification
import cloud.fabX.fabXaccess.user.application.AddingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.AddingPinIdentity
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.AddingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.AddingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
import cloud.fabX.fabXaccess.user.application.ChangingPassword
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.DeletingUser
import cloud.fabX.fabXaccess.user.application.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserIdByWikiName
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.application.RemovingInstructorQualification
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.application.RemovingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.RemovingPinIdentity
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.RemovingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.WebauthnIdentityService
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class UserController(
    private val gettingUser: GettingUser,
    private val gettingUserIdByWikiName: GettingUserIdByWikiName,
    private val addingUser: AddingUser,
    private val changingUser: ChangingUser,
    private val deletingUser: DeletingUser,
    private val softDeletedUsers: GettingSoftDeletedUsers,
    private val changingIsAdmin: ChangingIsAdmin,
    private val addingInstructorQualification: AddingInstructorQualification,
    private val removingInstructorQualification: RemovingInstructorQualification,
    private val addingMemberQualification: AddingMemberQualification,
    private val removingMemberQualification: RemovingMemberQualification,
    private val addingUsernamePasswordIdentity: AddingUsernamePasswordIdentity,
    private val changingPassword: ChangingPassword,
    private val removingUsernamePasswordIdentity: RemovingUsernamePasswordIdentity,
    private val addingWebauthnIdentity: AddingWebauthnIdentity,
    private val removingWebauthnIdentity: RemovingWebauthnIdentity,
    private val addingCardIdentity: AddingCardIdentity,
    private val removingCardIdentity: RemovingCardIdentity,
    private val addingPhoneNrIdentity: AddingPhoneNrIdentity,
    private val removingPhoneNrIdentity: RemovingPhoneNrIdentity,
    private val addingPinIdentity: AddingPinIdentity,
    private val removingPinIdentity: RemovingPinIdentity,
    private val webauthnService: WebauthnIdentityService
) {

    val routes: Route.() -> Unit = {
        route("/user") {
            get("") {
                call.respondWithErrorHandler(
                    readAdminAuthentication()
                        .map { admin ->
                            gettingUser
                                .getAll(
                                    admin,
                                    newCorrelationId()
                                )
                                .map { it.toRestModel() }
                        }
                )
            }

            get("/{id}") {
                readUUIDParameter("id")
                    ?.let { UserId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    gettingUser
                                        .getById(
                                            admin,
                                            newCorrelationId(),
                                            id
                                        )
                                        .map { it.toRestModel() }
                                }
                        )
                    }
            }

            get("/me") {
                call.respondWithErrorHandler(
                    readMemberAuthentication()
                        .flatMap { member ->
                            gettingUser.getMe(member, newCorrelationId())
                                .map { it.toRestModel() }
                        }
                )
            }

            get("/id-by-wiki-name") {
                requireStringQueryParameter("wikiName")?.let {
                    call.respondWithErrorHandler(
                        readInstructorAuthentication()
                            .flatMap { instructor ->
                                gettingUserIdByWikiName.getUserIdByWikiName(
                                    instructor,
                                    newCorrelationId(),
                                    it
                                )
                            }
                            .map { it.serialize() }
                    )
                }
            }

            post("") {
                readBody<UserCreationDetails>()?.let {
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                addingUser.addUser(
                                    admin,
                                    newCorrelationId(),
                                    it.firstName,
                                    it.lastName,
                                    it.wikiName
                                )
                            }
                            .map { it.serialize() }
                    )
                }
            }

            put("/{id}") {
                readBody<UserDetails>()?.let {
                    readUUIDParameter("id")
                        ?.let { UserId(it) }
                        ?.let { id ->
                            call.respondWithErrorHandler(
                                readAdminAuthentication()
                                    .flatMap { admin ->
                                        changingUser.changePersonalInformation(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            it.firstName.toDomain(),
                                            it.lastName.toDomain(),
                                            it.wikiName.toDomain(),
                                        )
                                            .toEither { }
                                            .swap()
                                    }
                            )
                        }
                }
            }

            put("/{id}/lock") {
                readBody<UserLockDetails>()?.let {
                    readUUIDParameter("id")
                        ?.let { UserId(it) }
                        ?.let { id ->
                            call.respondWithErrorHandler(
                                readAdminAuthentication()
                                    .flatMap { admin ->
                                        changingUser.changeLockState(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            it.locked.toDomain(),
                                            it.notes.toDomain()
                                        )
                                            .toEither { }
                                            .swap()
                                    }
                            )
                        }
                }
            }

            delete("/{id}") {
                readUUIDParameter("id")
                    ?.let { UserId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    deletingUser.deleteUser(
                                        admin,
                                        newCorrelationId(),
                                        id
                                    )
                                        .toEither { }
                                        .swap()
                                }
                        )
                    }
            }

            put("/{id}/is-admin") {
                readBody<IsAdminDetails>()
                    ?.let {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                call.respondWithErrorHandler(
                                    readAdminAuthentication()
                                        .flatMap { admin ->
                                            changingIsAdmin.changeIsAdmin(
                                                admin,
                                                newCorrelationId(),
                                                id,
                                                it.isAdmin
                                            )
                                                .toEither { }
                                                .swap()
                                        }
                                )
                            }
                    }
            }

            route("/{id}/instructor-qualification") {
                post("") {
                    readBody<QualificationAdditionDetails>()
                        ?.let {
                            readUUIDParameter("id")
                                ?.let { UserId(it) }
                                ?.let { id ->
                                    call.respondWithErrorHandler(
                                        readAdminAuthentication()
                                            .flatMap { admin ->
                                                addingInstructorQualification.addInstructorQualification(
                                                    admin,
                                                    newCorrelationId(),
                                                    id,
                                                    QualificationId.fromString(it.qualificationId)
                                                )
                                                    .toEither { }
                                                    .swap()
                                            }
                                    )
                                }
                        }
                }

                delete("/{qualificationId}") {
                    readUUIDParameter("id")
                        ?.let { UserId(it) }
                        ?.let { id ->
                            readUUIDParameter("qualificationId")
                                ?.let { QualificationId(it) }
                                ?.let { qualificationId ->
                                    call.respondWithErrorHandler(
                                        readAdminAuthentication()
                                            .flatMap { admin ->
                                                removingInstructorQualification.removeInstructorQualification(
                                                    admin,
                                                    newCorrelationId(),
                                                    id,
                                                    qualificationId
                                                )
                                                    .toEither { }
                                                    .swap()
                                            }
                                    )
                                }
                        }
                }
            }

            route("/{id}/member-qualification") {
                post("") {
                    readBody<QualificationAdditionDetails>()
                        ?.let {
                            readUUIDParameter("id")
                                ?.let { UserId(it) }
                                ?.let { id ->
                                    call.respondWithErrorHandler(
                                        readInstructorAuthentication()
                                            .flatMap { instructor ->
                                                addingMemberQualification.addMemberQualification(
                                                    instructor,
                                                    newCorrelationId(),
                                                    id,
                                                    QualificationId.fromString(it.qualificationId)
                                                )
                                                    .toEither { }
                                                    .swap()
                                            }
                                    )
                                }
                        }
                }

                delete("/{qualificationId}") {
                    readUUIDParameter("id")
                        ?.let { UserId(it) }
                        ?.let { id ->
                            readUUIDParameter("qualificationId")
                                ?.let { QualificationId(it) }
                                ?.let { qualificationId ->
                                    call.respondWithErrorHandler(
                                        readAdminAuthentication()
                                            .flatMap { admin ->
                                                removingMemberQualification.removeMemberQualification(
                                                    admin,
                                                    newCorrelationId(),
                                                    id,
                                                    qualificationId
                                                )
                                                    .toEither { }
                                                    .swap()
                                            }
                                    )
                                }
                        }
                }
            }

            route("/{id}/identity") {
                route("/username-password") {
                    post("") {
                        readBody<UsernamePasswordIdentityAdditionDetails>()
                            ?.let {
                                readUUIDParameter("id")
                                    ?.let { UserId(it) }
                                    ?.let { id ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    addingUsernamePasswordIdentity.addUsernamePasswordIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        it.username,
                                                        hash(it.password)
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }

                    post("/change-password") {
                        readBody<PasswordChangeDetails>()
                            ?.let {
                                readUUIDParameter("id")
                                    ?.let { UserId(it) }
                                    ?.let { id ->
                                        call.respondWithErrorHandler(
                                            readMemberAuthentication()
                                                .flatMap { member ->
                                                    changingPassword.changeOwnPassword(
                                                        member,
                                                        newCorrelationId(),
                                                        id,
                                                        hash(it.password)
                                                    )
                                                }
                                        )
                                    }
                            }
                    }

                    delete("/{username}") {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                readStringParameter("username")
                                    ?.let { username ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    removingUsernamePasswordIdentity.removeUsernamePasswordIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        username
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }
                }

                route("/webauthn") {
                    post("/register") {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                call.respondWithErrorHandler(
                                    readMemberAuthentication()
                                        .flatMap { member ->
                                            webauthnService.getNewChallenge(id)
                                                .map { challenge ->
                                                    WebauthnRegistrationDetails(
                                                        "direct",
                                                        challenge,
                                                        webauthnService.rpId,
                                                        webauthnService.rpName,
                                                        member.userId.value.toByteArray(),
                                                        member.name,
                                                        member.name,
                                                        webauthnService.pubKeyCredParams.map {
                                                            PubKeyCredParamEntry(it.type.value, it.alg.value)
                                                        }
                                                    )
                                                }
                                        }
                                )
                            }
                    }
                    post("/response") {
                        readBody<WebauthnIdentityAdditionDetails>()
                            ?.let {
                                readUUIDParameter("id")
                                    ?.let { UserId(it) }
                                    ?.let { id ->
                                        call.respondWithErrorHandler(
                                            readMemberAuthentication()
                                                .flatMap { member ->
                                                    addingWebauthnIdentity.addWebauthnIdentity(
                                                        member,
                                                        newCorrelationId(),
                                                        id,
                                                        it.attestationObject,
                                                        it.clientDataJSON
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }
                    delete("/{credentialId}") {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                readHexStringParameter("credentialId")
                                    ?.let { credentialId ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    removingWebauthnIdentity.removeWebauthnIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        credentialId
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }
                }

                route("/card") {
                    post("") {
                        readBody<CardIdentity>()
                            ?.let {
                                readUUIDParameter("id")
                                    ?.let { UserId(it) }
                                    ?.let { id ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    addingCardIdentity.addCardIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        it.cardId,
                                                        it.cardSecret
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }

                    delete("/{cardId}") {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                readStringParameter("cardId")
                                    ?.let { cardId ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    removingCardIdentity.removeCardIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        cardId
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }
                }

                route("/phone") {
                    post("") {
                        readBody<PhoneNrIdentity>()
                            ?.let {
                                readUUIDParameter("id")
                                    ?.let { UserId(it) }
                                    ?.let { id ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    addingPhoneNrIdentity.addPhoneNrIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        it.phoneNr
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }

                    delete("/{phoneNr}") {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                readStringParameter("phoneNr")
                                    ?.let { phoneNr ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    removingPhoneNrIdentity.removePhoneNrIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        phoneNr
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }
                }

                route("/pin") {
                    post("") {
                        readBody<PinIdentityDetails>()
                            ?.let {
                                readUUIDParameter("id")
                                    ?.let { UserId(it) }
                                    ?.let { id ->
                                        call.respondWithErrorHandler(
                                            readAdminAuthentication()
                                                .flatMap { admin ->
                                                    addingPinIdentity.addPinIdentity(
                                                        admin,
                                                        newCorrelationId(),
                                                        id,
                                                        it.pin
                                                    )
                                                        .toEither { }
                                                        .swap()
                                                }
                                        )
                                    }
                            }
                    }

                    delete("") {
                        readUUIDParameter("id")
                            ?.let { UserId(it) }
                            ?.let { id ->
                                call.respondWithErrorHandler(
                                    readAdminAuthentication()
                                        .flatMap { admin ->
                                            removingPinIdentity.removePinIdentity(
                                                admin,
                                                newCorrelationId(),
                                                id
                                            )
                                                .toEither { }
                                                .swap()
                                        }
                                )
                            }
                    }
                }
            }

            route("/soft-deleted") {
                get("") {
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .map { admin ->
                                softDeletedUsers
                                    .getSoftDeletedUsers(
                                        admin,
                                        newCorrelationId()
                                    )
                                    .map { it.toRestModel() }
                            }
                    )
                }

                delete("/{id}") {
                    readUUIDParameter("id")
                        ?.let { UserId(it) }
                        ?.let { id ->
                            call.respondWithErrorHandler(
                                readAdminAuthentication()
                                    .flatMap { admin ->
                                        deletingUser.hardDeleteUser(
                                            admin,
                                            newCorrelationId(),
                                            id
                                        )
                                    }
                            )
                        }
                }
            }
        }
    }
}