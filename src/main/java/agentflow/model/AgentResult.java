package agentflow.model;

import java.util.Collections;
import java.util.Map;

/**
 * Result payload returned after an Agent completes its execution cycle.
 */
public record AgentResult(
        boolean success,
        AgentRole role,
        String message,
        Map<String, Object> outputData
) {
    public AgentResult {
        outputData = outputData != null ? Collections.unmodifiableMap(outputData) : Collections.emptyMap();
    }

    public static AgentResult ok(AgentRole role, String message) {
        return new AgentResult(true, role, message, Collections.emptyMap());
    }

    public static AgentResult ok(AgentRole role, String message, Map<String, Object> output) {
        return new AgentResult(true, role, message, output);
    }

    public static AgentResult error(AgentRole role, String message) {
        return new AgentResult(false, role, message, Collections.emptyMap());
    }
}
