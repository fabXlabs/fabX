package cloud.fabX.fabXaccess

class App {
    private val log = logger()

    fun start() {
        log.trace("hello world trace")
        log.debug("hello world debug")
        log.info("hello world info")
        log.warn("hello world warn")
        log.error("hello world error")

        RestModule.start()
    }
}

fun main() {
    App().start()
}