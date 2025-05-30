package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.model.newDeviceId
import cloud.fabX.fabXaccess.common.model.newQualificationId
import cloud.fabX.fabXaccess.common.model.newToolId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.device.application.AddingCardIdentityAtDevice
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.ChangingFirmwareVersion
import cloud.fabX.fabXaccess.device.application.ChangingThumbnail
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.DeviceDomainEventHandler
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.application.GettingDevicePinStatus
import cloud.fabX.fabXaccess.device.application.RestartingDevice
import cloud.fabX.fabXaccess.device.application.UnlockingTool
import cloud.fabX.fabXaccess.device.application.UpdatingDeviceFirmware
import cloud.fabX.fabXaccess.device.application.UpdatingDevicePinStatus
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.ChangingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.tool.application.AddingTool
import cloud.fabX.fabXaccess.tool.application.ChangingTool
import cloud.fabX.fabXaccess.tool.application.DeletingTool
import cloud.fabX.fabXaccess.tool.application.GettingTool
import cloud.fabX.fabXaccess.user.application.AddingCardIdentity
import cloud.fabX.fabXaccess.user.application.AddingInstructorQualification
import cloud.fabX.fabXaccess.user.application.AddingMemberQualification
import cloud.fabX.fabXaccess.user.application.AddingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.AddingPinIdentity
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.AddingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.AddingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
import cloud.fabX.fabXaccess.user.application.ChangingPassword
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.DeletingUser
import cloud.fabX.fabXaccess.user.application.GettingAuthorizedTools
import cloud.fabX.fabXaccess.user.application.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.application.GettingUserIdByWikiName
import cloud.fabX.fabXaccess.user.application.GettingUserSourcingEvents
import cloud.fabX.fabXaccess.user.application.LoggingUnlockedTool
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.application.RemovingInstructorQualification
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.application.RemovingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.RemovingPinIdentity
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.RemovingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.UserDomainEventHandler
import cloud.fabX.fabXaccess.user.application.UserMetrics
import cloud.fabX.fabXaccess.user.application.ValidatingSecondFactor
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import cloud.fabX.fabXaccess.tool.application.ChangingThumbnail as ChangingToolThumbnail

val domainModule = DI.Module("domain") {
    // device
    bindSingleton { AddingCardIdentityAtDevice(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { AddingDevice(instance(), instance(), instance(), instance()) }
    bindSingleton { AttachingTool(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingDevice(instance(), instance(), instance()) }
    bindSingleton { ChangingThumbnail(instance(), instance()) }
    bindSingleton { ChangingFirmwareVersion(instance(), instance(), instance()) }
    bindSingleton { DeletingDevice(instance(), instance(), instance()) }
    bindSingleton { DetachingTool(instance(), instance(), instance()) }
    bindSingleton { DeviceDomainEventHandler(instance(), instance(), instance()) }
    bindSingleton {
        GettingConfiguration(
            instance(),
            instance(),
            instance(),
            instance(tag = "gettingConfigurationCounter"),
            instance()
        )
    }
    bindSingleton { GettingDevice(instance(), instance()) }
    bindSingleton { RestartingDevice(instance(), instance(), instance()) }
    bindSingleton {
        UnlockingTool(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(tag = "toolUsageCounter")
        )
    }
    bindSingleton { UpdatingDeviceFirmware(instance(), instance(), instance()) }
    bindSingleton { GettingDevicePinStatus(instance(), instance()) }
    bindSingleton { UpdatingDevicePinStatus(instance(), instance()) }

    // qualification
    bindSingleton { AddingQualification(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingQualification(instance(), instance(), instance()) }
    bindSingleton { DeletingQualification(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { GettingQualification(instance(), instance()) }

    // tool
    bindSingleton { AddingTool(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingTool(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingToolThumbnail(instance(), instance()) }
    bindSingleton { DeletingTool(instance(), instance(), instance(), instance()) }
    bindSingleton { GettingTool(instance(), instance()) }

    // user
    bindSingleton { AddingCardIdentity(instance(), instance(), instance(), instance()) }
    bindSingleton { AddingInstructorQualification(instance(), instance(), instance(), instance()) }
    bindSingleton { AddingMemberQualification(instance(), instance(), instance(), instance()) }
    bindSingleton { AddingPhoneNrIdentity(instance(), instance(), instance(), instance()) }
    bindSingleton { AddingPinIdentity(instance(), instance(), instance()) }
    bindSingleton { AddingUser(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { AddingUsernamePasswordIdentity(instance(), instance(), instance(), instance()) }
    bindSingleton { AddingWebauthnIdentity(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingIsAdmin(instance(), instance(), instance()) }
    bindSingleton { ChangingPassword(instance(), instance(), instance()) }
    bindSingleton { ChangingUser(instance(), instance(), instance(), instance()) }
    bindSingleton { DeletingUser(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { GettingAuthorizedTools(instance(), instance(), instance()) }
    bindSingleton { GettingSoftDeletedUsers(instance(), instance()) }
    bindSingleton { GettingUser(instance(), instance()) }
    bindSingleton { GettingUserByIdentity(instance(), instance()) }
    bindSingleton { GettingUserIdByWikiName(instance(), instance()) }
    bindSingleton { GettingUserSourcingEvents(instance(), instance()) }
    bindSingleton {
        LoggingUnlockedTool(
            instance(tag = "toolUsageCounter"),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton { RemovingCardIdentity(instance(), instance(), instance()) }
    bindSingleton { RemovingInstructorQualification(instance(), instance(), instance()) }
    bindSingleton { RemovingMemberQualification(instance(), instance(), instance()) }
    bindSingleton { RemovingPhoneNrIdentity(instance(), instance(), instance()) }
    bindSingleton { RemovingPinIdentity(instance(), instance(), instance()) }
    bindSingleton { RemovingUsernamePasswordIdentity(instance(), instance(), instance()) }
    bindSingleton { RemovingWebauthnIdentity(instance(), instance(), instance()) }
    bindSingleton {
        UserDomainEventHandler(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton { UserMetrics(instance()) }
    bindSingleton { ValidatingSecondFactor(instance(), instance()) }

    bindSingleton { { newDeviceId() } }
    bindSingleton { { newQualificationId() } }
    bindSingleton { { newToolId() } }
    bindSingleton { { newUserId() } }
}