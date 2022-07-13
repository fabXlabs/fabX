package cloud.fabX.fabXaccess.common.model

object ErrorFixture {

    fun arbitraryError(): Error = Error.VersionConflict("some message")

}