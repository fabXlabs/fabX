package cloud.fabX.fabXaccess.qualification.model

object QualificationFixture {

    fun arbitrary(
        qualificationId: QualificationId = QualificationIdFixture.arbitrary(),
        aggregateVersion: Long = 1,
        name: String = "qualification",
        description: String = "description",
        colour: String = "#000000",
        orderNr: Int = 1
    ): Qualification = Qualification(
        qualificationId,
        aggregateVersion,
        name,
        description,
        colour,
        orderNr
    )
}