import interceptors.NamedMiddleware
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterceptorTests {

    @Test
    @DisplayName("Add named middleware class")
    @Order(1)
    fun `add named middleware class`() {
        val middlewareName = Nexus.interceptors.middleware(null, NamedMiddleware)
        Nexus.logger.info(middlewareName)

        assertEquals("pw.mihou.middleware", middlewareName, "Middleware name doesn't match intended name.")
        assertTrue(NexusCommandInterceptorCore.has("pw.mihou.middleware"), "Middleware wasn't added.")
    }
}