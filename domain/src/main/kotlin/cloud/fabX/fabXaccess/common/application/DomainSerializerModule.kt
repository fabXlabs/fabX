package cloud.fabX.fabXaccess.common.application

import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.UserId
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val domainSerializersModule = SerializersModule {
    polymorphic(ActorId::class) {
        subclass(UserId::class)
        subclass(SystemActorId::class)
    }
}