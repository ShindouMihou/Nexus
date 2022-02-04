package pw.mihou.nexus.core.reflective.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to tell Nexus to inject either global middleware
 * or afterware into this list.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectGlobal {

    /**
     * Should we inject the global middleware
     * or afterware in this list?
     *
     * @return Is this an injection for middleware or afterware?
     */
    boolean middleware();

}
