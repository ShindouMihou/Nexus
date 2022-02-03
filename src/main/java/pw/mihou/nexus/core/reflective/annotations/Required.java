package pw.mihou.nexus.core.reflective.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This tells {@link pw.mihou.nexus.core.reflective.core.NexusReflectiveVariableCore} that this
 * field is an required field which means it cannot be null.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Required {
}
