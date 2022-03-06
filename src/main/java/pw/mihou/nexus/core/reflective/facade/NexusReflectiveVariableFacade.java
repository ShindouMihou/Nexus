package pw.mihou.nexus.core.reflective.facade;

import java.util.Map;
import java.util.Optional;

/**
 * This facade is dedicated to {@link pw.mihou.nexus.core.reflective.core.NexusReflectiveVariableCore} which
 * is utilized by {@link pw.mihou.nexus.core.reflective.NexusReflectiveCore} to grab variables with reflection
 * and so forth while abiding by the rules of default values.
 */
public interface NexusReflectiveVariableFacade {

    /**
     * Gets the value of the field with the specified name.
     *
     * @param field The field name to fetch.
     * @param <R> The type to expect returned.
     * @return The field value if present.
     */
    <R> Optional<R> get(String field);

    /**
     * Gets the map containing all the shared fields.
     *
     * @return  An unmodifiable map that contains all the shared
     * fields that were defined in the class.
     */
    Map<String, Object> getSharedFields();

    /**
     * Gets the value of the field with the specified name that
     * matches the specific type.
     *
     * @param field The field name to fetch.
     * @param rClass The type in class to expect.
     * @param <R> The type to expect returned.
     * @return The field value if present and also matches the type.
     */
    @SuppressWarnings("unchecked")
    default <R> Optional<R> getWithType(String field, Class<R> rClass) {
        return get(field).filter(o ->
                rClass.isAssignableFrom(o.getClass())
                || o.getClass().equals(rClass)
        ).map(o -> (R) o);
    }

}
