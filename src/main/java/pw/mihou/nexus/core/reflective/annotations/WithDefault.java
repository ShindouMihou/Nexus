package pw.mihou.nexus.core.reflective.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An internal annotation that tells {@link pw.mihou.nexus.core.reflective.NexusReflectiveCore} to utilize
 * whatever value is set on the variable if there isn't any value specified on the user's side.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface WithDefault {
}
