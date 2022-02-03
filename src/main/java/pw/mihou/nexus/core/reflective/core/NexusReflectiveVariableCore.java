package pw.mihou.nexus.core.reflective.core;

import pw.mihou.nexus.core.reflective.annotations.Required;
import pw.mihou.nexus.core.reflective.annotations.WithDefault;
import pw.mihou.nexus.core.reflective.facade.NexusReflectiveVariableFacade;

import java.util.*;

public class NexusReflectiveVariableCore implements NexusReflectiveVariableFacade {

    private final HashMap<String, Object> fields = new HashMap<>();

    public NexusReflectiveVariableCore(Object object, Class<?> clazz) {
            // We'll collect all the fields with the WithDefault annotation from the reference class first.
            // then utilize those fields when we need a default value. Please ensure that the field always
            // has a value beforehand.
        try {
            Object t = clazz.newInstance();

            Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(WithDefault.class))
                    .peek(field -> field.setAccessible(true))
                    .forEach(field -> {
                        try {
                            fields.put(field.getName().toLowerCase(), field.get(t));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new IllegalStateException(
                                    "Nexus was unable to complete variable reflection stage for class: " + clazz.getName()
                            );
                        }
                    });

            // After collecting all the defaults, we can start bootstrapping the fields HashMap.
            Arrays.stream(object.getClass().getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);

                try {
                    Object obj = field.get(object);
                    
                    if (obj == null) {
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

            // This implements the requirements of the Required annotation which tells the
            // user that a certain field is required before compilation. Field safety.
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Required.class))
                    .forEach(field -> {
                        field.setAccessible(true);
                        if (!fields.containsKey(field.getName())) {
                            throw new IllegalStateException(
                                    "Nexus was unable to complete variable reflection stage for class: " + object.getClass().getName() +
                                            " because the field: " + field.getName() + " is required to have a value."
                            );
                        }
                    });
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Optional<R> get(String field) {
        return fields.containsKey(field.toLowerCase()) ? Optional.of((R) fields.get(field.toLowerCase())) : Optional.empty();

    }
}
