package agentflow.strategy;

import agentflow.model.WorkflowContext;
import agentflow.tool.Tool;
import agentflow.tool.ToolResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Execution strategy simulating external cloud/microservice REST API interactions.
 * Injects simulated network overhead, HTTP request/response headers, and API telemetry.
 */
public class SimulatedApiStrategy implements ExecutionStrategy {
    private final int simulatedNetworkLatencyMs;

    public SimulatedApiStrategy(int simulatedNetworkLatencyMs) {
        this.simulatedNetworkLatencyMs = simulatedNetworkLatencyMs;
    }

    public SimulatedApiStrategy() {
        this(250);
    }

    @Override
    public String getName() {
        return "Simulated Cloud API";
    }

    @Override
    public String getDescription() {
        return "Dispatches tool commands as simulated REST microservice calls with simulated HTTP payload transport";
    }

    @Override
    public ToolResult execute(Tool tool, Map<String, Object> parameters, WorkflowContext context) {
        long start = System.currentTimeMillis();
        String requestId = "req-" + UUID.randomUUID().toString().substring(0, 8);

        if (simulatedNetworkLatencyMs > 0) {
            try {
                // Add minor random variance to simulate network jitter
                long jitter = (long) (Math.random() * 50);
                Thread.sleep(simulatedNetworkLatencyMs + jitter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ToolResult result = tool.execute(parameters);

        Map<String, Object> enrichedData = new HashMap<>(result.data());
        enrichedData.put("api_requestId", requestId);
        enrichedData.put("api_status", result.success() ? 200 : 500);
        enrichedData.put("api_protocol", "HTTP/2 (Simulated)");

        String enrichedOutput = String.format("[API Endpoint: https://api.agentic.internal/v1/tools/%s | ReqId: %s | Status: %s]\n%s",
                tool.getName(), requestId, result.success() ? "200 OK" : "500 Error",
                result.success() ? result.output() : result.error());

        return new ToolResult(result.success(), enrichedOutput, result.error(), enrichedData, System.currentTimeMillis() - start);
    }
}
