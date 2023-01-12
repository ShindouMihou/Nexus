package pw.mihou.nexus.core.reflective.core;

import pw.mihou.nexus.core.exceptions.NotInheritableException;
import pw.mihou.nexus.core.reflective.annotations.Required;
import pw.mihou.nexus.core.reflective.annotations.Share;
import pw.mihou.nexus.core.reflective.annotations.WithDefault;
import pw.mihou.nexus.core.reflective.facade.NexusReflectiveVariableFacade;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.inheritance.Inherits;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class NexusReflectiveVariableCore implements NexusReflectiveVariableFacade {

    private final HashMap<String, Object> fields = new HashMap<>();
    private final HashMap<String, Object> sharedFields = new HashMap<>();

    public NexusReflectiveVariableCore(Object object, NexusCommandCore core) {
        // We'll collect all the fields with the WithDefault annotation from the reference class first.
        // then utilize those fields when we need a default value. Please ensure that the field always
        // has a value beforehand.

        Arrays.stream(NexusCommandCore.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(WithDefault.class))
                .peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    try {
                        fields.put(field.getName().toLowerCase(), field.get(core));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new IllegalStateException(
                                "Nexus was unable to complete variable reflection stage for class: " + NexusCommandCore.class.getName()
                        );
                    }
                });

        if (object.getClass().isAnnotationPresent(Inherits.class)) {
            Class<?> parent = object.getClass().getAnnotation(Inherits.class).value();
            Constructor<?> constructor = Arrays.stream(parent.getConstructors())
                    .filter(construct -> construct.getParameterCount() == 0)
                    .findFirst()
                    .orElse(null);

            if (constructor == null) {
                throw new NotInheritableException(parent);
            }

            try {
                Object inheritanceReference = constructor.newInstance();
                Arrays.stream(parent.getDeclaredFields())
                        .forEach(field -> {
                            field.setAccessible(true);
                            try {
                                fields.put(field.getName().toLowerCase(), field.get(inheritanceReference));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                throw new IllegalStateException("Nexus was unable to complete variable inheritance stage for class: " + object.getClass().getName());
                            }
                        });
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // After collecting all the defaults, we can start bootstrapping the fields HashMap.
        Arrays.stream(object.getClass().getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);

            try {
                Object obj = field.get(object);

                if (obj == null) {
                    return;
                }

                if (field.isAnnotationPresent(Share.class)) {
                    sharedFields.put(field.getName().toLowerCase(), obj);
                    return;
                }

                fields.put(field.getName().toLowerCase(), obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalStateException(
                        "Nexus was unable to complete variable reflection stage for class: " + object.getClass().getName()
                );
            }
        });

        // Handling required fields, the difference between `clazz` and `object.getClass()`
        // is that `clazz` refers to the NexusCommandImplementation while `object` refers
        // to the developer-defined object.
        Arrays.stream(NexusCommandCore.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Required.class))
                .forEach(field -> {
                    @Nullable Object obj = fields.get(field.getName().toLowerCase());
                    if (obj == null) {
                        throw new IllegalStateException(
                                "Nexus was unable to complete variable reflection stage for class: " + object.getClass().getName() +
                                        " because the field: " + field.getName() + " is required to have a value."
                        );
                    }
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Optional<R> get(String field) {
        return fields.containsKey(field.toLowerCase()) ? Optional.of((R) fields.get(field.toLowerCase())) : Optional.empty();

    }

    @Override
    public Map<String, Object> getSharedFields() {
        return Collections.unmodifiableMap(sharedFields);
    }
}
