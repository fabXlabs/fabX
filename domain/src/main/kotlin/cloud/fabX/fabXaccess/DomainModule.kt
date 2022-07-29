package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.model.newDeviceId
import cloud.fabX.fabXaccess.common.model.newQualificationId
import cloud.fabX.fabXaccess.common.model.newToolId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.DeviceDomainEventHandler
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.application.GettingDevice
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
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.AddingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.DeletingUser
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.application.RemovingInstructorQualification
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.application.RemovingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.UserDomainEventHandler
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val domainModule = DI.Module("domain") {
    // device
    bindSingleton { AddingDevice(instance(), instance(), instance(), instance()) }
    bindSingleton { AttachingTool(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingDevice(instance(), instance(), instance()) }
    bindSingleton { DeletingDevice(instance(), instance(), instance()) }
    bindSingleton { DetachingTool(instance(), instance(), instance()) }
    bindSingleton { DeviceDomainEventHandler(instance(), instance(), instance()) }
    bindSingleton { GettingConfiguration(instance(), instance(), instance()) }
    bindSingleton { GettingDevice(instance(), instance()) }

    // qualification
    bindSingleton { AddingQualification(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingQualification(instance(), instance(), instance()) }
    bindSingleton { DeletingQualification(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton { GettingQualification(instance(), instance()) }

    // tool
    bindSingleton { AddingTool(instance(), instance(), instance(), instance()) }
    bindSingleton { ChangingTool(instance(), instance(), instance()) }
    bindSingleton { DeletingTool(instance(), instance(), instance(), instance()) }
    bindSingleton { GettingTool(instance(), instance()) }

    // user
    bindSingleton { AddingCardIdentity(instance(), instance(), instance()) }
    bindSingleton { AddingInstructorQualification(instance(), instance(), instance()) }
    bindSingleton { AddingMemberQualification(instance(), instance(), instance()) }
    bindSingleton { AddingPhoneNrIdentity(instance(), instance(), instance()) }
    bindSingleton { AddingUser(instance(), instance(), instance(), instance()) }
    bindSingleton { AddingUsernamePasswordIdentity(instance(), instance(), instance()) }
    bindSingleton { ChangingIsAdmin(instance(), instance()) }
    bindSingleton { ChangingUser(instance(), instance(), instance()) }
    bindSingleton { DeletingUser(instance(), instance()) }
    bindSingleton { GettingUser(instance(), instance()) }
    bindSingleton { GettingUserByIdentity(instance(), instance()) }
    bindSingleton { RemovingCardIdentity(instance(), instance()) }
    bindSingleton { RemovingInstructorQualification(instance(), instance()) }
    bindSingleton { RemovingMemberQualification(instance(), instance()) }
    bindSingleton { RemovingPhoneNrIdentity(instance(), instance()) }
    bindSingleton { RemovingUsernamePasswordIdentity(instance(), instance()) }
    bindSingleton { UserDomainEventHandler(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton { { newDeviceId() } }
    bindSingleton { { newQualificationId() } }
    bindSingleton { { newToolId() } }
    bindSingleton { { newUserId() } }
}