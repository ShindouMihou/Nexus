package pw.mihou.nexus.features.command.interceptors.commons;

/**
 * This class exists purely to separate the static function of adding
 * all the common interceptors. You can use this as a reference for all built-in Nexus middlewares
 * and afterwares.
 */
public class NexusCommonInterceptors {

    public static final String NEXUS_GATE_SERVER = "nexus.gate.server";
    public static final String NEXUS_GATE_DMS = "nexus.gate.dms";
    public static final String NEXUS_RATELIMITER = "nexus.ratelimiter";

}
