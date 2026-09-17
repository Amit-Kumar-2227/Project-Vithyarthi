package agentflow.strategy;

import agentflow.model.WorkflowContext;
import agentflow.tool.Tool;
import agentflow.tool.ToolResult;

import java.util.Map;

/**
 * Strategy pattern interface for dynamic task execution behavior.
 * Decouples how a tool command is executed (e.g., direct deterministic, remote API simulation, fault-tolerant retry).
 */
public interface ExecutionStrategy {

    /**
     * Human-readable identifier for this strategy.
     */
    String getName();

    /**
     * Detailed description of execution mechanics.
     */
    String getDescription();

    /**
     * Executes the given tool under this strategy's specific execution semantics.
     *
     * @param tool       the target tool to execute
     * @param parameters arguments passed to the tool
     * @param context    the active workflow context
     * @return result of tool execution
     */
    ToolResult execute(Tool tool, Map<String, Object> parameters, WorkflowContext context);
}
