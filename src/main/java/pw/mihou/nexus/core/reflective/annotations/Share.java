package pw.mihou.nexus.core.reflective.annotations;

import pw.mihou.nexus.features.command.core.NexusCommandDispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells the reflective engine to share this field inside the
 * command store which stores all the external variables not defined
 * in {@link NexusCommandDispatcher}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Share {
}
