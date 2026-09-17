package agentflow.tool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and discovery service for tools available to Executor agents.
 */
public class ToolRegistry {
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    public ToolRegistry() {
        registerDefaultTools();
    }

    private void registerDefaultTools() {
        register(new WebSearchTool());
        register(new FileSystemTool());
        register(new DataProcessorTool());
        register(new CodeAnalysisTool());
        register(new SystemNotificationTool());
    }

    public void register(Tool tool) {
        if (tool != null) {
            tools.put(tool.getName().toLowerCase(), tool);
        }
    }

    public Optional<Tool> getTool(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(tools.get(name.toLowerCase()));
    }

    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    public boolean hasTool(String name) {
        return name != null && tools.containsKey(name.toLowerCase());
    }
}
