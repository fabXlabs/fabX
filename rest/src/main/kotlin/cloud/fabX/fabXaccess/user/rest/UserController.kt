package cloud.fabX.fabXaccess.user.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.GettingUser
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route

class UserController(
    private val gettingUser: GettingUser,
    private val addingUser: AddingUser,
    private val changingUser: ChangingUser
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
            // TODO change user lock state
            // TODO delete user
        }
    }
}