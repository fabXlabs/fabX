package cloud.fabX.fabXaccess.common.model

object ErrorFixture {

    fun arbitrary(): Error = Error.VersionConflict("some message")

}