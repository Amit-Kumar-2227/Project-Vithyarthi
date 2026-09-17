package agentflow.strategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and manager for execution strategies.
 * Allows users and agents to dynamically toggle execution strategies at runtime.
 */
public class StrategyRegistry {
    private final Map<String, ExecutionStrategy> strategies = new ConcurrentHashMap<>();
    private volatile ExecutionStrategy activeStrategy;

    public StrategyRegistry() {
        ExecutionStrategy deterministic = new DeterministicExecutionStrategy();
        ExecutionStrategy api = new SimulatedApiStrategy();
        ExecutionStrategy fallback = new FaultTolerantFallbackStrategy();

        strategies.put("deterministic", deterministic);
        strategies.put("api", api);
        strategies.put("fallback", fallback);

        this.activeStrategy = deterministic;
    }

    public void register(String key, ExecutionStrategy strategy) {
        if (key != null && strategy != null) {
            strategies.put(key.toLowerCase(), strategy);
        }
    }

    public ExecutionStrategy getActiveStrategy() {
        return activeStrategy;
    }

    public void setActiveStrategy(String key) {
        if (key != null && strategies.containsKey(key.toLowerCase())) {
            this.activeStrategy = strategies.get(key.toLowerCase());
        } else {
            throw new IllegalArgumentException("Unknown strategy: " + key);
        }
    }

    public void setActiveStrategy(ExecutionStrategy strategy) {
        this.activeStrategy = Objects.requireNonNull(strategy, "strategy cannot be null");
    }

    public Map<String, ExecutionStrategy> getAllStrategies() {
        return Collections.unmodifiableMap(strategies);
    }
}
