package pw.mihou.nexus.core.enginex.event.media;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * A temporary session storage space for any {@link pw.mihou.nexus.core.enginex.event.NexusEngineEvent} to store
 * any form of data received from the event.
 */
public record NexusEngineEventWriteableStore(
        Map<String, Object> data
) implements NexusEngineEventReadableStore {

    /**
     * Writes data into the temporary store that can be accessed through the
     * {@link NexusEngineEventReadableStore}.
     *
     * @param key   The key name of the data.
     * @param value The value name of the data.
     */
    public void write(String key, Object value) {
        data.put(key, value);
    }

    @Nullable
    @Override
    public Object get(String key) {
        return data.get(key);
    }
}
