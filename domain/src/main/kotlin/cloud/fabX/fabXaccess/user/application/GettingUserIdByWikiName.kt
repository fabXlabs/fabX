package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.Instructor

/**
 * Service to get user ids by wiki name.
 *
 * Allows instructors to get user ids of members they want to add member qualifications to without revealing personal
 * details about the member.
 */
class GettingUserIdByWikiName(
    loggerFactory: LoggerFactory,
    private val gettingUserByWikiName: GettingUserByWikiName
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun getUserIdByWikiName(
        actor: Instructor,
        correlationId: CorrelationId,
        wikiName: String
    ): Either<Error, UserId> {
        log.debug("getUserIdByWikiName (actor: $actor, correlationId: $correlationId)...")

        return gettingUserByWikiName.getByWikiName(wikiName)
            .map { it.id }
            .tap { log.debug("...getUserIdByWikiName done") }
            .tapLeft { log.error("...getUserIdByWikiName error: $it") }
    }
}