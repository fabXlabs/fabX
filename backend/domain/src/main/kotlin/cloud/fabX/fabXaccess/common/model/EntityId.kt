package cloud.fabX.fabXaccess.common.model

interface EntityId<out IdType> {
    val value: IdType
}