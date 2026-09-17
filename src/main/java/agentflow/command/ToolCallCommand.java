package agentflow.command;

import agentflow.core.StateMachine;
import agentflow.model.AgentRole;
import agentflow.model.ExecutionLogEntry;
import agentflow.model.Task;
import agentflow.model.TaskStatus;
import agentflow.model.WorkflowContext;
import agentflow.strategy.ExecutionStrategy;
import agentflow.tool.Tool;
import agentflow.tool.ToolResult;

import java.util.Map;
import java.util.Objects;

/**
 * Concrete Command encapsulating the invocation of a Tool via an ExecutionStrategy.
 * Manages task state transitions and compensation/rollback logic.
 */
public class ToolCallCommand implements TaskCommand {
    private final Task task;
    private final Tool tool;
    private final ExecutionStrategy strategy;
    private final WorkflowContext context;
    private final StateMachine stateMachine;
    private long executionTimeMs = 0;

    public ToolCallCommand(Task task, Tool tool, ExecutionStrategy strategy,
                           WorkflowContext context, StateMachine stateMachine) {
        this.task = Objects.requireNonNull(task, "task cannot be null");
        this.tool = Objects.requireNonNull(tool, "tool cannot be null");
        this.strategy = Objects.requireNonNull(strategy, "strategy cannot be null");
        this.context = Objects.requireNonNull(context, "context cannot be null");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine cannot be null");
    }

    @Override
    public boolean execute() {
        long start = System.currentTimeMillis();
        stateMachine.transitionTask(context, task, TaskStatus.RUNNING, AgentRole.EXECUTOR,
                String.format("Executing via [%s] strategy using tool '%s'", strategy.getName(), tool.getName()));

        try {
            Map<String, Object> params = task.getParameters();
            ToolResult result = strategy.execute(tool, params, context);
            executionTimeMs = System.currentTimeMillis() - start;

            if (result.success()) {
                task.setResult(result.output());
                // Save output to context memory if relevant
                context.setMemoryValue("result_" + task.getId(), result.output());
                stateMachine.transitionTask(context, task, TaskStatus.COMPLETED, AgentRole.EXECUTOR,
                        String.format("Tool completed in %d ms", executionTimeMs));
                return true;
            } else {
                task.setErrorMessage(result.error());
                stateMachine.transitionTask(context, task, TaskStatus.FAILED, AgentRole.EXECUTOR,
                        "Tool execution returned error: " + result.error());
                return false;
            }
        } catch (Exception e) {
            executionTimeMs = System.currentTimeMillis() - start;
            task.setErrorMessage(e.getMessage());
            stateMachine.transitionTask(context, task, TaskStatus.FAILED, AgentRole.EXECUTOR,
                    "Unexpected execution exception: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void undo() {
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.FAILED) {
            if (tool.canUndo()) {
                stateMachine.notifyLog(context, ExecutionLogEntry.info(AgentRole.EXECUTOR,
                        String.format("Compensating/Rolling back task [%s] via tool '%s'", task.getId(), tool.getName())));
                tool.undo(task.getParameters());
            }
            stateMachine.transitionTask(context, task, TaskStatus.ROLLED_BACK, AgentRole.EXECUTOR,
                    "Command rolled back by compensating action");
        }
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public TaskStatus getStatus() {
        return task.getStatus();
    }

    @Override
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Tool getTool() {
        return tool;
    }

    public ExecutionStrategy getStrategy() {
        return strategy;
    }
}
