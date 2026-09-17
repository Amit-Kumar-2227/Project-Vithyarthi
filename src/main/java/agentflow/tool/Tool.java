package agentflow.tool;

import java.util.Map;

/**
 * Interface representing an executable pseudo-tool that agents can invoke.
 */
public interface Tool {

    /**
     * Unique identifier/name of the tool (e.g., "web_search", "file_system").
     */
    String getName();

    /**
     * Human-readable summary of what the tool accomplishes.
     */
    String getDescription();

    /**
     * Map of required parameter names to their type descriptions.
     */
    Map<String, String> getParameterSchema();

    /**
     * Executes the tool with the given arguments.
     */
    ToolResult execute(Map<String, Object> parameters);

    /**
     * Optional compensation action to undo or roll back changes made by this tool.
     */
    default boolean canUndo() {
        return false;
    }

    default void undo(Map<String, Object> parameters) {
        // Default no-op for read-only tools
    }
}
