package pw.mihou.nexus.core.reflective.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This injects a UUID specifically for this field, it should be
 * unique and can be used for other external stuff. This is useable only from
 * {@link pw.mihou.nexus.core.reflective.NexusReflectiveCore} and nothing else.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectUUID {
}
