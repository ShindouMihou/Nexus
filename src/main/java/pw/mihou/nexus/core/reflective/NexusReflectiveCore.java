package pw.mihou.nexus.core.reflective;

import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.reflective.annotations.*;
import pw.mihou.nexus.core.reflective.core.NexusReflectiveVariableCore;
import pw.mihou.nexus.core.reflective.facade.NexusReflectiveFacade;
import pw.mihou.nexus.core.reflective.facade.NexusReflectiveVariableFacade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NexusReflectiveCore {

    public static final HashMap<Class<?>, NexusReflectiveFacade<?>> adapters = new HashMap<>();

    public static <R> R accept(Object object, Class<R> rClass, NexusCore core) {
        // This requires having NOT a single constructor. Please note to any contributors.
        NexusReflectiveVariableFacade facade = new NexusReflectiveVariableCore(object, rClass);
        try {
            if (rClass.isAnnotationPresent(MustImplement.class)) {
                Class<?> extension = rClass.getAnnotation(MustImplement.class).clazz();

                if (!extension.isAssignableFrom(object.getClass())) {
                    throw new IllegalStateException("Nexus was unable to complete reflection stage because class: " +
                            object.getClass().getName()
                            + " must implement the following class: " + extension.getName());
                }
            }

            R r = rClass.newInstance();

            Arrays.stream(r.getClass().getDeclaredFields())
                    .forEach(field -> {
                        field.setAccessible(true);

                        try {
                            if (field.isAnnotationPresent(InjectReferenceClass.class)) {
                                field.set(r, object);
                            } else if (field.isAnnotationPresent(InjectUUID.class)) {
                                field.set(r, UUID.randomUUID().toString());
                            } else if (field.isAnnotationPresent(InjectNexusCore.class)) {
                                field.set(r, core);
                            } else {
                                facade.getWithType(field.getName(), fromPrimitiveToNonPrimitive(field.getType())).ifPresent(o -> {
                                    try {
                                        if (field.isAnnotationPresent(InjectGlobal.class)) {
                                            InjectGlobal injectGlobal = field.getAnnotation(InjectGlobal.class);

                                            if (injectGlobal.middleware()) {
                                                ((List<String>) o).addAll(core.getGlobalMiddlewares());
                                            } else {
                                                ((List<String>) o).addAll(core.getGlobalAfterwares());
                                            }

                                        }
                                        field.set(r, o);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

            if (rClass.isAnnotationPresent(InvokeIfAnnotated.class)) {
                InvokeIfAnnotated annotation = rClass.getAnnotation(InvokeIfAnnotated.class);

                if (object.getClass().isAnnotationPresent(annotation.annotation())) {
                    // If InvokeIfAnnotated is up and the referencing class has the annotation required then we invoke the method.
                    Method method = annotation.invokingClass().getDeclaredMethod(annotation.methodName(), rClass);
                    method.setAccessible(true);
                    method.invoke(annotation.invokingClass(), r);
                }
            }
            return r;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // If it ever errors out, please read the very first comment of this method.
            // If it errors the NoSuchMethodException then there is something wrong with the invoking method.
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This identifies primitive classes and returns back their non-primitive
     * class values since for some reason, type-checking requires it.
     *
     * @param clazz The class to identify.
     * @return The non-primitive class variant.
     */
    private static Class<?> fromPrimitiveToNonPrimitive(Class<?> clazz) {
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
            return Boolean.class;
        else if (clazz.equals(Integer.class) || clazz.equals(int.class))
            return Integer.class;
        else if (clazz.equals(Long.class) || clazz.equals(long.class))
            return Long.class;
        else if (clazz.equals(Character.class) || clazz.equals(char.class))
            return Character.class;
        else if (clazz.equals(String.class))
            return String.class;
        else if (clazz.equals(Double.class) || clazz.equals(double.class))
            return Double.class;
        else
            return clazz;
    }

}
