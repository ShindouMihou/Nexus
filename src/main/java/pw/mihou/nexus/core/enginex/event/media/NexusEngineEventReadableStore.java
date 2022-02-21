package pw.mihou.nexus.core.enginex.event.media;

import javax.annotation.Nullable;

public interface NexusEngineEventReadableStore {

    @Nullable
    Object get(String key);

    @Nullable
    default String getString(String key) {
        return as(key, String.class);
    }

    @Nullable
    default Boolean getBoolean(String key) {
        return as(key, Boolean.class);
    }

    @Nullable
    default Integer getInteger(String key) {
        return as(key, Integer.class);
    }

    @Nullable
    default Long getLong(String key) {
        return as(key, Long.class);
    }

    @Nullable
    default Double getDouble(String key) {
        return as(key, Double.class);
    }

    /**
     * Checks if the type for this class is of the specified class and
     * returns the value if it is otherwise returns null.
     *
     * @param key           The key of the data to acquire.
     * @param typeClass     The type of class that this object is required to be.
     * @param <NextType>    The type of class that this object is required to be.
     * @return              The object value if it matches the requirements.
     */
    @Nullable
    default <NextType> NextType as(String key, Class<NextType> typeClass) {
        Object value = get(key);

        if (typeClass.isInstance(value)) {
            return typeClass.cast(value);
        }

        return null;
    }

}
