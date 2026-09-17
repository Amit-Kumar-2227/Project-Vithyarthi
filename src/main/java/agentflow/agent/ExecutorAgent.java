package agentflow.agent;

import agentflow.command.CommandHistory;
import agentflow.command.ToolCallCommand;
import agentflow.core.StateMachine;
import agentflow.model.*;
import agentflow.strategy.ExecutionStrategy;
import agentflow.strategy.StrategyRegistry;
import agentflow.tool.Tool;
import agentflow.tool.ToolRegistry;

import java.util.*;

/**
 * Executor Agent: Coordinates task command instantiation, dynamic strategy assignment,
 * dependency checking, and execution orchestration.
 */
public class ExecutorAgent implements Agent {
    private final String name;
    private final ToolRegistry toolRegistry;
    private final StrategyRegistry strategyRegistry;
    private final CommandHistory commandHistory;

    public ExecutorAgent(String name, ToolRegistry toolRegistry, StrategyRegistry strategyRegistry, CommandHistory commandHistory) {
        this.name = name;
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry cannot be null");
        this.strategyRegistry = Objects.requireNonNull(strategyRegistry, "strategyRegistry cannot be null");
        this.commandHistory = Objects.requireNonNull(commandHistory, "commandHistory cannot be null");
    }

    public ExecutorAgent(ToolRegistry toolRegistry, StrategyRegistry strategyRegistry, CommandHistory commandHistory) {
        this("Aegis-Executor-1", toolRegistry, strategyRegistry, commandHistory);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.EXECUTOR;
    }

    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    @Override
    public AgentResult process(WorkflowContext context, StateMachine stateMachine) {
        ExecutionStrategy currentStrategy = strategyRegistry.getActiveStrategy();
        stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                String.format("Starting task execution pipeline using Strategy: [%s]", currentStrategy.getName())));

        List<Task> tasks = new ArrayList<>(context.getTasks());
        int executedCount = 0;
        int failedCount = 0;

        for (Task task : tasks) {
            // Check if already completed (e.g. from previous runs or iterations)
            if (task.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }

            // Dependency check
            boolean dependenciesSatisfied = true;
            for (String depId : task.getDependencies()) {
                Optional<Task> depTask = context.getTaskById(depId);
                if (depTask.isEmpty() || depTask.get().getStatus() != TaskStatus.COMPLETED) {
                    dependenciesSatisfied = false;
                    break;
                }
            }

            if (!dependenciesSatisfied) {
                stateMachine.transitionTask(context, task, TaskStatus.SKIPPED, getRole(),
                        "Unsatisfied dependencies: " + task.getDependencies());
                continue;
            }

            // Resolve required tool
            Optional<Tool> toolOpt = toolRegistry.getTool(task.getRequiredTool());
            if (toolOpt.isEmpty()) {
                stateMachine.transitionTask(context, task, TaskStatus.FAILED, getRole(),
                        "Required tool not registered: " + task.getRequiredTool());
                failedCount++;
                continue;
            }

            Tool tool = toolOpt.get();

            // Instantiate ToolCallCommand (Command Pattern)
            ToolCallCommand command = new ToolCallCommand(task, tool, currentStrategy, context, stateMachine);
            commandHistory.record(command);

            boolean success = command.execute();
            if (success) {
                executedCount++;
            } else {
                failedCount++;
                stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                        "Command failed for task [" + task.getId() + "]. Halting pipeline or attempting rollback."));
                break;
            }
        }

        if (failedCount > 0) {
            return AgentResult.error(getRole(),
                    String.format("Execution completed with %d successes and %d failures.", executedCount, failedCount));
        }

        return AgentResult.ok(getRole(),
                String.format("Successfully executed %d tasks.", executedCount),
                Map.of("executedCount", executedCount));
    }
}
