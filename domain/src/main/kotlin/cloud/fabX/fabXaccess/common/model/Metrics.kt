package cloud.fabX.fabXaccess.common.model

interface Counter {
    fun increment(double: Double = 1.0)
}

interface TaggedCounter<T> {
    suspend fun increment(key: T, tags: suspend () -> Map<String, String>, double: Double = 1.0)
}