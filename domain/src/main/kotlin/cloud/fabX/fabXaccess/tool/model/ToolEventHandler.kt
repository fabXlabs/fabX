package cloud.fabX.fabXaccess.tool.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.valueToChangeTo

internal class ToolEventHandler : ToolSourcingEvent.EventHandler {
    override fun handle(event: ToolCreated, tool: Option<Tool>): Option<Tool> {
        if (tool.isDefined()) {
            throw AccumulatorNotEmptyForToolCreatedEventHandler(
                "Handler for ToolCreated is given $tool."
            )
        }

        return Some(
            Tool(
                id = event.aggregateRootId,
                aggregateVersion = event.aggregateVersion,
                name = event.name,
                type = event.toolType,
                requires2FA = event.requires2FA,
                time = event.time,
                idleState = event.idleState,
                enabled = true,
                wikiLink = event.wikiLink,
                requiredQualifications = event.requiredQualifications,
            )
        )
    }

    override fun handle(
        event: ToolDetailsChanged,
        tool: Option<Tool>
    ): Option<Tool> = requireSomeToolWithSameIdAnd(event, tool) { e, t ->
        Some(
            t.copy(
                aggregateVersion = e.aggregateVersion,
                name = e.name.valueToChangeTo(t.name),
                type = e.toolType.valueToChangeTo(t.type),
                requires2FA = e.requires2FA.valueToChangeTo(t.requires2FA),
                time = e.time.valueToChangeTo(t.time),
                idleState = e.idleState.valueToChangeTo(t.idleState),
                enabled = e.enabled.valueToChangeTo(t.enabled),
                wikiLink = e.wikiLink.valueToChangeTo(t.wikiLink),
                requiredQualifications = e.requiredQualifications.valueToChangeTo(t.requiredQualifications),
            )
        )
    }

    override fun handle(
        event: ToolDeleted,
        tool: Option<Tool>
    ): Option<Tool> = requireSomeToolWithSameIdAnd(event, tool) { _, _ ->
        None
    }

    private fun <E : ToolSourcingEvent> requireSomeToolWithSameIdAnd(
        event: E,
        tool: Option<Tool>,
        and: (E, Tool) -> Option<Tool>
    ): Option<Tool> {
        if (tool.map { it.id != event.aggregateRootId }.getOrElse { false }) {
            throw EventAggregateRootIdDoesNotMatchToolId(
                "Event $event cannot be applied to $tool. Aggregate root id does not match."
            )
        }

        return tool.flatMap { and(event, it) }
    }

    class EventAggregateRootIdDoesNotMatchToolId(message: String) : Exception(message)
    class AccumulatorNotEmptyForToolCreatedEventHandler(message: String) : Exception(message)
}