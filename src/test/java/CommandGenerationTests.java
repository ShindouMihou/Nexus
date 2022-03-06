import commands.FilledRequiredTestCommand;
import commands.HasMiddlewaresCommand;
import commands.HasSharedFieldsCommand;
import commands.RequiredTestCommand;
import org.junit.jupiter.api.*;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors;

import static org.junit.jupiter.api.Assertions.*;

public class CommandGenerationTests {

    private static final Nexus NEXUS = Nexus.builder().build();

    @Test
    @DisplayName("Enforcement of Required Fields Test")
    @Order(1)
    void isRequiredFieldsRequiredTest() {
        assertThrows(IllegalStateException.class, () -> NEXUS.createCommandFrom(new RequiredTestCommand()));
    }

    @Test
    @DisplayName("Nexus Command Generation Test")
    @Order(2)
    void commandGenerationTest() {
        NexusCommand command = NEXUS.createCommandFrom(new FilledRequiredTestCommand());
        assertNotNull(
                command,
                "The command instance which shouldn't be null is somehow null."
        );
    }

    @Test
    @DisplayName("Value Validation of Required Fields Test")
    @Order(3)
    void requiredFieldsTest() {
        NexusCommand command = NEXUS.createCommandFrom(new FilledRequiredTestCommand());
        assertNotNull(
                command.getName(),
                "The command name which shouldn't be null is somehow null."
        );

        assertNotNull(
                command.getDescription(),
                "The command description which shouldn't be null is somehow null."
        );

        assertTrue(
                command.getName().equals("fulfilled") && command.getDescription().equals("fulfilled"),
                "The value of the command name or description does not match the value of the class."
        );
    }

    @Test
    @DisplayName("Injected Nexus Core Test")
    @Order(4)
    void injectedNexusCoreTest() {
        NexusCommand command = NEXUS.createCommandFrom(new FilledRequiredTestCommand());
        assertNotNull(
                ((NexusCommandCore) command).core,
                "The injection of the Nexus core somehow failed."
        );
    }

    @Test
    @DisplayName("Successful Command Addition Test")
    @Order(5)
    void commandAddedTest() {
        NexusCommand command = NEXUS.createCommandFrom(new FilledRequiredTestCommand());
        assertTrue(
                NEXUS.getCommandManager().getCommandByUUID(((NexusCommandCore) command).uuid).isPresent(),
                "The command was not found in the command manager despite being added."
        );
    }

    @Test
    @DisplayName("Correct Shared Fields Test")
    @Order(6)
    void correctSharedFieldsTest() {
        NexusCommand command = NEXUS.createCommandFrom(new HasSharedFieldsCommand());
        assertTrue(
                command.get("oneSharedField", String.class).isPresent(),
                "The shared field is somehow not visible to the public."
        );

        assertTrue(
                command.get("oneNonSharedField", String.class).isEmpty(),
                "The non-shared field is somehow visible to the public."
        );

        assertEquals(
                command.get("oneSharedField", String.class).get(),
                "This should be visible to middlewares and afterwares.",
                "The shared field's value does not match the intended value."
        );
    }

    @Test
    @DisplayName("Has Middleware Test")
    @Order(7)
    void hasMiddlewareTest() {
        NexusCommand command = NEXUS.createCommandFrom(new HasMiddlewaresCommand());
        assertFalse(
                ((NexusCommandCore) command).middlewares.isEmpty(),
                "The middlewares field which shouldn't be empty is somehow empty."
        );
        assertEquals(
                ((NexusCommandCore) command).middlewares.get(0),
                NexusCommonInterceptors.NEXUS_RATELIMITER,
                "The middleware does not match the intended value."
        );
    }

    @Test
    @DisplayName("Has Middleware Test")
    @Order(8)
    void hasAfterwareTest() {
        NexusCommand command = NEXUS.createCommandFrom(new HasMiddlewaresCommand());
        assertFalse(
                ((NexusCommandCore) command).afterwares.isEmpty(),
                "The afterwares field which shouldn't be empty is somehow empty."
        );
        assertEquals(
                ((NexusCommandCore) command).afterwares.get(0),
                NexusCommonInterceptors.NEXUS_RATELIMITER,
                "The afterwares does not match the intended value."
        );
    }


}
