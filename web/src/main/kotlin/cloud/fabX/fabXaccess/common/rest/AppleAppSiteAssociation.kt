package cloud.fabX.fabXaccess.common.rest

import kotlinx.serialization.Serializable

@Serializable
data class AppleAppSiteAssociation(
    val webcredentials: WebCredentials
)

@Serializable
data class WebCredentials(
    val apps: List<String>
)