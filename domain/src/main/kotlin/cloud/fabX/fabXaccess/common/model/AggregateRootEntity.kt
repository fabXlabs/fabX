package cloud.fabX.fabXaccess.common.model

interface AggregateRootEntity<IdType>: Entity<IdType> where IdType: EntityId<Any> {
    val aggregateVersion: Long
}