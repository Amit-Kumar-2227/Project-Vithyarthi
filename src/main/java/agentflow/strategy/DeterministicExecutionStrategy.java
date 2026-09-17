package agentflow.strategy;

import agentflow.model.WorkflowContext;
import agentflow.tool.Tool;
import agentflow.tool.ToolResult;

import java.util.Map;

/**
 * Standard deterministic execution strategy.
 * Direct, fast invocation with slight simulated latency to simulate realistic agent thought/processing time.
 */
public class DeterministicExecutionStrategy implements ExecutionStrategy {
    private final int simulatedDelayMs;

    public DeterministicExecutionStrategy(int simulatedDelayMs) {
        this.simulatedDelayMs = Math.max(0, simulatedDelayMs);
    }

    public DeterministicExecutionStrategy() {
        this(120);
    }

    @Override
    public String getName() {
        return "Deterministic (Direct)";
    }

    @Override
    public String getDescription() {
        return "Executes tools directly with deterministic outcomes and calibrated processing latency";
    }

    @Override
    public ToolResult execute(Tool tool, Map<String, Object> parameters, WorkflowContext context) {
        if (simulatedDelayMs > 0) {
            try {
                Thread.sleep(simulatedDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return tool.execute(parameters);
    }
}
