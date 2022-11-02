package pw.mihou.nexus.core.reflective;

import pw.mihou.nexus.core.reflective.annotations.*;
import pw.mihou.nexus.core.reflective.core.NexusReflectiveVariableCore;
import pw.mihou.nexus.core.reflective.facade.NexusReflectiveVariableFacade;
import pw.mihou.nexus.features.command.annotation.IdentifiableAs;
import pw.mihou.nexus.features.command.core.NexusCommandCore;

import java.util.*;

public class NexusReflectiveCore {

    private static final Class<?> REFERENCE_CLASS = NexusCommandCore.class;

    public static NexusCommandCore command(Object object) {
        NexusCommandCore reference = new NexusCommandCore();

        NexusReflectiveVariableFacade facade = new NexusReflectiveVariableCore(object, reference);

        if (REFERENCE_CLASS.isAnnotationPresent(MustImplement.class)) {
            Class<?> extension = REFERENCE_CLASS.getAnnotation(MustImplement.class).clazz();

            if (!extension.isAssignableFrom(object.getClass())) {
                throw new IllegalStateException("Nexus was unable to complete reflection stage because class: " +
                        object.getClass().getName()
                        + " must implement the following class: " + extension.getName());
            }
        }

        String temporaryUuid = facade.getWithType("name", String.class).orElseThrow();

        if (object.getClass().isAnnotationPresent(IdentifiableAs.class)) {
            temporaryUuid = object.getClass().getAnnotation(IdentifiableAs.class).key();
        }

        final String uuid = temporaryUuid;

        Arrays.stream(reference.getClass().getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);

                    try {
                        if (field.isAnnotationPresent(InjectReferenceClass.class)) {
                            field.set(reference, object);
                        } else if (field.isAnnotationPresent(InjectUUID.class)) {
                            field.set(reference, uuid);
                        } else if (field.isAnnotationPresent(Stronghold.class)){
                            field.set(reference, facade.getSharedFields());
                        } else {
                            facade.getWithType(field.getName(), fromPrimitiveToNonPrimitive(field.getType())).ifPresent(o -> {
                                try {
                                    field.set(reference, o);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

        return reference;
    }

    /**
     * This identifies primitive classes and returns their non-primitive
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
