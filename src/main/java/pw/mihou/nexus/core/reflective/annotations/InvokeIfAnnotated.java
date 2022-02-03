package pw.mihou.nexus.core.reflective.annotations;

import java.lang.annotation.*;

/**
 * This invokes the method on the invoking class with the created class as the parameter
 * if the object it is referencing from has the specific annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InvokeIfAnnotated {

    Class<? extends Annotation> annotation();
    Class<?> invokingClass();
    String methodName();

}
