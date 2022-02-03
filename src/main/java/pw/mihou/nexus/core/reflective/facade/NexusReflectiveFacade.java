package pw.mihou.nexus.core.reflective.facade;

import java.util.HashMap;

public interface NexusReflectiveFacade<R> {

    /**
     * This allows the {@link pw.mihou.nexus.core.reflective.NexusReflectiveCore} to create custom
     * classes whenever needed.
     *
     * @param variables The variables that is contained.
     * @return The class to be transformed into.
     */
    R transform(HashMap<String, Object> variables);

}
