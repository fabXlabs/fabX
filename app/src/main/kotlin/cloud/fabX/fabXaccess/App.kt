package cloud.fabX.fabXaccess

import kotlin.system.exitProcess

class App {
    private val log = logger()

    fun helloWorld() {
        log.trace("hello world trace")
        log.debug("hello world debug")
        log.info("hello world info")
        log.warn("hello world warn")
        log.error("hello world error")

        if (!DomainModule.isFullyConfigured()) {
            log.error("DomainModule not fully configured!")
            exitProcess(-1)
        }
    }
}

fun main() {
    App().helloWorld()
}