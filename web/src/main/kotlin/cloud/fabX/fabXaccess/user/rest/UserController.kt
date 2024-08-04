package cloud.fabX.fabXaccess.user.rest

import arrow.core.right
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readHexStringParameter
import cloud.fabX.fabXaccess.common.rest.readStringParameter
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.requireStringQueryParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toByteArray
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.common.rest.withAdminAuthRespond
import cloud.fabX.fabXaccess.common.rest.withAdminOrMemberAuthRespond
import cloud.fabX.fabXaccess.common.rest.withInstructorAuthRespond
import cloud.fabX.fabXaccess.common.rest.withMemberAuthRespond
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
import cloud.fabX.fabXaccess.user.application.GettingUserSourcingEvents
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.application.RemovingInstructorQualification
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.application.RemovingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.RemovingPinIdentity
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.RemovingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.WebauthnIdentityService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.util.pipeline.PipelineContext

class UserController(
    private val gettingUser: GettingUser,
    private val gettingUserIdByWikiName: GettingUserIdByWikiName,
    private val gettingUserSourcingEvents: GettingUserSourcingEvents,
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
                readId { id ->
                    withAdminAuthRespond { admin ->
                        gettingUser
                            .getById(
                                admin,
                                newCorrelationId(),
                                id
                            )
                            .map { it.toRestModel() }
                    }
                }
            }

            get("/me") {
                withMemberAuthRespond { member ->
                    gettingUser.getMe(member, newCorrelationId())
                        .map { it.toRestModel() }
                }
            }

            get("/id-by-wiki-name") {
                requireStringQueryParameter("wikiName")?.let {
                    withInstructorAuthRespond { instructor ->
                        gettingUserIdByWikiName
                            .getUserIdByWikiName(
                                instructor,
                                newCorrelationId(),
                                it
                            )
                            .map { it.serialize() }
                    }
                }
            }

            get("/sourcing-event") {
                withAdminAuthRespond { admin ->
                    gettingUserSourcingEvents
                        .getAll(admin, newCorrelationId())
                        .right()
                }
            }

            get("/{id}/sourcing-event") {
                readId { id ->
                    withAdminAuthRespond { admin ->
                        gettingUserSourcingEvents
                            .getById(admin, newCorrelationId(), id)
                    }
                }
            }

            post("") {
                readBody<UserCreationDetails>()?.let {
                    withAdminAuthRespond { admin ->
                        addingUser
                            .addUser(
                                admin,
                                newCorrelationId(),
                                it.firstName,
                                it.lastName,
                                it.wikiName
                            )
                            .map { it.serialize() }
                    }
                }
            }

            put("/{id}") {
                readBody<UserDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingUser.changePersonalInformation(
                                admin,
                                newCorrelationId(),
                                id,
                                it.firstName.toDomain(),
                                it.lastName.toDomain(),
                                it.wikiName.toDomain(),
                            )
                        }
                    }
                }
            }

            put("/{id}/lock") {
                readBody<UserLockDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingUser.changeLockState(
                                admin,
                                newCorrelationId(),
                                id,
                                it.locked.toDomain(),
                                it.notes.toDomain()
                            )
                        }
                    }
                }
            }

            delete("/{id}") {
                readId { id ->
                    withAdminAuthRespond { admin ->
                        deletingUser.deleteUser(
                            admin,
                            newCorrelationId(),
                            id
                        )
                    }
                }
            }

            put("/{id}/is-admin") {
                readBody<IsAdminDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingIsAdmin.changeIsAdmin(
                                admin,
                                newCorrelationId(),
                                id,
                                it.isAdmin
                            )
                        }
                    }
                }
            }

            route("/{id}/instructor-qualification") {
                post("") {
                    readBody<QualificationAdditionDetails>()?.let {
                        readId { id ->
                            withAdminAuthRespond { admin ->
                                addingInstructorQualification.addInstructorQualification(
                                    admin,
                                    newCorrelationId(),
                                    id,
                                    QualificationId.fromString(it.qualificationId)
                                )
                            }
                        }
                    }
                }

                delete("/{qualificationId}") {
                    readId { id ->
                        readUUIDParameter("qualificationId")
                            ?.let { QualificationId(it) }
                            ?.let { qualificationId ->
                                withAdminAuthRespond { admin ->
                                    removingInstructorQualification.removeInstructorQualification(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        qualificationId
                                    )
                                }
                            }
                    }
                }
            }

            route("/{id}/member-qualification") {
                post("") {
                    readBody<QualificationAdditionDetails>()?.let {
                        readId { id ->
                            withInstructorAuthRespond { instructor ->
                                addingMemberQualification.addMemberQualification(
                                    instructor,
                                    newCorrelationId(),
                                    id,
                                    QualificationId.fromString(it.qualificationId)
                                )
                            }
                        }
                    }
                }

                delete("/{qualificationId}") {
                    readId { id ->
                        readUUIDParameter("qualificationId")
                            ?.let { QualificationId(it) }
                            ?.let { qualificationId ->
                                withAdminAuthRespond { admin ->
                                    removingMemberQualification.removeMemberQualification(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        qualificationId
                                    )
                                }
                            }
                    }
                }
            }

            route("/{id}/identity") {
                route("/username-password") {
                    post("") {
                        readBody<UsernamePasswordIdentityAdditionDetails>()?.let {
                            readId { id ->
                                withAdminAuthRespond { admin ->
                                    addingUsernamePasswordIdentity.addUsernamePasswordIdentity(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        it.username,
                                        hash(it.password)
                                    )
                                }
                            }
                        }
                    }

                    post("/change-password") {
                        readBody<PasswordChangeDetails>()?.let {
                            readId { id ->
                                withMemberAuthRespond { member ->
                                    changingPassword.changeOwnPassword(
                                        member,
                                        newCorrelationId(),
                                        id,
                                        hash(it.password)
                                    )
                                }
                            }
                        }
                    }

                    delete("/{username}") {
                        readId { id ->
                            readStringParameter("username")
                                ?.let { username ->
                                    withAdminAuthRespond { admin ->
                                        removingUsernamePasswordIdentity.removeUsernamePasswordIdentity(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            username
                                        )
                                    }
                                }
                        }
                    }
                }

                route("/webauthn") {
                    post("/register") {
                        readId { id ->
                            withMemberAuthRespond { member ->
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
                        }
                    }
                    post("/response") {
                        readBody<WebauthnIdentityAdditionDetails>()?.let {
                            readId { id ->
                                withMemberAuthRespond { member ->
                                    addingWebauthnIdentity.addWebauthnIdentity(
                                        member,
                                        newCorrelationId(),
                                        id,
                                        it.attestationObject,
                                        it.clientDataJSON
                                    )
                                }
                            }
                        }
                    }
                    delete("/{credentialId}") {
                        val correlationId = newCorrelationId()
                        readId { id ->
                            readHexStringParameter("credentialId")
                                ?.let { credentialId ->
                                    withAdminOrMemberAuthRespond(
                                        { admin ->
                                            removingWebauthnIdentity.removeWebauthnIdentity(
                                                admin,
                                                correlationId,
                                                id,
                                                credentialId
                                            )
                                        },
                                        { member ->
                                            removingWebauthnIdentity.removeWebauthnIdentity(
                                                member,
                                                correlationId,
                                                id,
                                                credentialId
                                            )
                                        }
                                    )
                                }
                        }
                    }
                }

                route("/card") {
                    post("") {
                        readBody<CardIdentity>()?.let {
                            readId { id ->
                                withAdminAuthRespond { admin ->
                                    addingCardIdentity.addCardIdentity(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        it.cardId,
                                        it.cardSecret
                                    )
                                }
                            }
                        }
                    }

                    delete("/{cardId}") {
                        readId { id ->
                            readStringParameter("cardId")
                                ?.let { cardId ->
                                    withAdminAuthRespond { admin ->
                                        removingCardIdentity.removeCardIdentity(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            cardId
                                        )
                                    }
                                }
                        }
                    }
                }

                route("/phone") {
                    post("") {
                        readBody<PhoneNrIdentity>()?.let {
                            readId { id ->
                                withAdminAuthRespond { admin ->
                                    addingPhoneNrIdentity.addPhoneNrIdentity(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        it.phoneNr
                                    )
                                }
                            }
                        }
                    }

                    delete("/{phoneNr}") {
                        readId { id ->
                            readStringParameter("phoneNr")
                                ?.let { phoneNr ->
                                    withAdminAuthRespond { admin ->
                                        removingPhoneNrIdentity.removePhoneNrIdentity(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            phoneNr
                                        )
                                    }
                                }
                        }
                    }
                }

                route("/pin") {
                    post("") {
                        readBody<PinIdentityDetails>()?.let {
                            readId { id ->
                                withAdminAuthRespond { admin ->
                                    addingPinIdentity.addPinIdentity(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        it.pin
                                    )
                                }
                            }
                        }
                    }

                    delete("") {
                        readId { id ->
                            withAdminAuthRespond { admin ->
                                removingPinIdentity.removePinIdentity(
                                    admin,
                                    newCorrelationId(),
                                    id
                                )
                            }
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
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            deletingUser.hardDeleteUser(
                                admin,
                                newCorrelationId(),
                                id
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend inline fun PipelineContext<*, ApplicationCall>.readId(
        function: (UserId) -> Unit
    ) {
        readUUIDParameter("id")
            ?.let { UserId(it) }
            ?.let { id ->
                function(id)
            }
    }
}