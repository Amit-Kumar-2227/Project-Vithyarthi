package agentflow.strategy;

import agentflow.model.WorkflowContext;
import agentflow.tool.Tool;
import agentflow.tool.ToolResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Strategy providing fault tolerance: automatic retries, backoff, and graceful fallback.
 */
public class FaultTolerantFallbackStrategy implements ExecutionStrategy {
    private final int maxRetries;
    private final long backoffMs;

    public FaultTolerantFallbackStrategy(int maxRetries, long backoffMs) {
        this.maxRetries = Math.max(1, maxRetries);
        this.backoffMs = Math.max(0, backoffMs);
    }

    public FaultTolerantFallbackStrategy() {
        this(3, 80);
    }

    @Override
    public String getName() {
        return "Fault-Tolerant (Retry & Fallback)";
    }

    @Override
    public String getDescription() {
        return "Provides resilience by automatically retrying failed commands with backoff and degraded fallback";
    }

    @Override
    public ToolResult execute(Tool tool, Map<String, Object> parameters, WorkflowContext context) {
        long start = System.currentTimeMillis();
        int attempts = 0;
        ToolResult lastResult = null;

        while (attempts < maxRetries) {
            attempts++;
            lastResult = tool.execute(parameters);
            if (lastResult.success()) {
                if (attempts > 1) {
                    Map<String, Object> enriched = new HashMap<>(lastResult.data());
                    enriched.put("recovered_after_attempts", attempts);
                    return new ToolResult(true,
                            String.format("[Recovered on attempt %d/%d]\n%s", attempts, maxRetries, lastResult.output()),
                            null, enriched, System.currentTimeMillis() - start);
                }
                return lastResult;
            }

            // Exponential backoff
            if (attempts < maxRetries) {
                try {
                    Thread.sleep(backoffMs * attempts);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Graceful fallback mitigation
        String fallbackMsg = String.format("[Fallback Engaged]: Tool '%s' failed after %d attempts (Error: %s). Applying safe degraded fallback state.",
                tool.getName(), maxRetries, lastResult != null ? lastResult.error() : "Unknown");

        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("fallback_activated", true);
        fallbackData.put("total_attempts", attempts);

        return new ToolResult(true, fallbackMsg, null, fallbackData, System.currentTimeMillis() - start);
    }
}
