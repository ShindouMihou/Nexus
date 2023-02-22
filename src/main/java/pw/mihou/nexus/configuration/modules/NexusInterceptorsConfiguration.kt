package pw.mihou.nexus.configuration.modules

class NexusInterceptorsConfiguration internal constructor() {
    /**
     * Sets whether to defer middleware responses when the middlewares have reached the
     * [autoDeferMiddlewaresInMilliseconds] milliseconds.
     * This is only possible in middlewares because middlewares uses a custom method of responding.
     *
     * WARNING: You have to write your command to also use deferred responses. It is solely your responsibility to
     * ensure that whichever commands uses middlewares that would take longer than the specified [autoDeferMiddlewaresInMilliseconds]
     * has to use deferred responses.
     */
    @Volatile
    @set:JvmName("setAutoDeferMiddlewareResponses")
    @get:JvmName("autoDeferMiddlewareResponses")
    var autoDeferMiddlewareResponses = false

    @Volatile
    @set:JvmName("setAutoDeferMiddlewaresInMilliseconds")
    @get:JvmName("autoDeferMiddlewaresInMilliseconds")
    var autoDeferMiddlewaresInMilliseconds = 2500L

    @Volatile
    @set:JvmName("setAutoDeferAsEphemeral")
    @get:JvmName("autoDeferAsEphemeral")
    var autoDeferAsEphemeral = true
}