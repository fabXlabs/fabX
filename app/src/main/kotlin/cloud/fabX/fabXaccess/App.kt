package cloud.fabX.fabXaccess

class App {
    private val log = logger()

    fun start() {
        log.trace("hello world trace")
        log.debug("hello world debug")
        log.info("hello world info")
        log.warn("hello world warn")
        log.error("hello world error")

//        if (!DomainModule.isFullyConfigured()) {
//            log.error("DomainModule not fully configured!")
//            exitProcess(-1)
//        }

        val rest = RestConfiguration()
        rest.start()
    }
}

fun main() {
    App().start()
}