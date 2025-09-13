package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.UserIdFactory
import cloud.fabX.fabXaccess.tool.application.logError
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to add new users.
 */
@OptIn(ExperimentalTime::class)
class AddingUser(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByWikiName: GettingUserByWikiName,
    private val userIdFactory: UserIdFactory,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addUser(
        actor: Admin,
        correlationId: CorrelationId,
        firstName: String,
        lastName: String,
        wikiName: String
    ): Either<Error, UserId> =
        log.logError(actor, correlationId, "addUser") {
            User
                .addNew(
                    userIdFactory,
                    actor,
                    clock,
                    correlationId,
                    firstName,
                    lastName,
                    wikiName,
                    gettingUserByWikiName
                )
                .flatMap { sourcingEvent ->
                    userRepository.store(sourcingEvent)
                        .map { sourcingEvent.aggregateRootId }
                }
        }
}