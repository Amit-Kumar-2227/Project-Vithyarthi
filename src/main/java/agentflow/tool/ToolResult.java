package agentflow.tool;

import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates the execution output and diagnostics of a tool call.
 */
public record ToolResult(
        boolean success,
        String output,
        String error,
        Map<String, Object> data,
        long executionTimeMs
) {
    public ToolResult {
        data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
    }

    public static ToolResult success(String output, long executionTimeMs) {
        return new ToolResult(true, output, null, Collections.emptyMap(), executionTimeMs);
    }

    public static ToolResult success(String output, Map<String, Object> data, long executionTimeMs) {
        return new ToolResult(true, output, null, data, executionTimeMs);
    }

    public static ToolResult failure(String error, long executionTimeMs) {
        return new ToolResult(false, null, error, Collections.emptyMap(), executionTimeMs);
    }
}
