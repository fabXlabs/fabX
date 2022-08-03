package cloud.fabX.fabXaccess.qualification.rest

import cloud.fabX.fabXaccess.common.rest.ChangeableValue
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

@Serializable
data class QualificationDetails(
    val name: ChangeableValue<String>?,
    val description: ChangeableValue<String>?,
    val colour: ChangeableValue<String>?,
    val orderNr: ChangeableValue<Int>?,
)