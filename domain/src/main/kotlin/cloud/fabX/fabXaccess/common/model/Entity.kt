package cloud.fabX.fabXaccess.common.model

interface Entity<IdType> where IdType: EntityId<Any> {
    val id: IdType
}