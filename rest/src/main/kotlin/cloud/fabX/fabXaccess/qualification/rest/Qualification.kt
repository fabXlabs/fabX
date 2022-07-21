package cloud.fabX.fabXaccess.qualification.rest

import kotlinx.serialization.Serializable

@Serializable
data class Qualification(
    val id: String,
    val aggregateVersion: Long,
    val name: String,
    val description: String,
    val colour: String,
    val orderNr: Int
)

fun cloud.fabX.fabXaccess.qualification.model.Qualification.toRestModel() = Qualification(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    name = name,
    description = description,
    colour = colour,
    orderNr = orderNr
)

@Serializable
data class QualificationCreationDetails(
    val name: String,
    val description: String,
    val colour: String,
    val orderNr: Int
)