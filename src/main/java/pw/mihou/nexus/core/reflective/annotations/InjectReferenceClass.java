package pw.mihou.nexus.core.reflective.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This tells {@link pw.mihou.nexus.core.reflective.NexusReflectiveCore} to inject the reference
 * class for instance the model onto as the variable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectReferenceClass {
}
