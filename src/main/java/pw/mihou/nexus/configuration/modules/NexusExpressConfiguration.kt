package pw.mihou.nexus.configuration.modules

import java.time.Duration

class NexusExpressConfiguration internal constructor() {

    /**
     * Maximum timeout refers to the maximum amount of time that an express request should
     * be kept waiting for a shard to be active, once the timeout is reached, the requests
     * will be expired and cancelled.
     */
    @Volatile
    var maximumTimeout = Duration.ofMinutes(10)

    /**
     * Whether to show warnings regarding requests in Express that have expired.
     */
    @Volatile
    var showExpiredWarnings = false

}