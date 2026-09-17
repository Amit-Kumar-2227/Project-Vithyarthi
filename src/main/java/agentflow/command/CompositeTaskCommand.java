package agentflow.command;

import agentflow.model.Task;
import agentflow.model.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Composite Command pattern implementation.
 * Chains multiple TaskCommands into a transactional sequence with automatic rollback on failure.
 */
public class CompositeTaskCommand implements TaskCommand {
    private final Task compositeTask;
    private final List<TaskCommand> commands;
    private final Stack<TaskCommand> executedCommands = new Stack<>();
    private long totalExecutionTimeMs = 0;

    public CompositeTaskCommand(Task compositeTask, List<TaskCommand> commands) {
        this.compositeTask = compositeTask;
        this.commands = new ArrayList<>(commands);
    }

    public void addCommand(TaskCommand cmd) {
        if (cmd != null) {
            commands.add(cmd);
        }
    }

    @Override
    public boolean execute() {
        long start = System.currentTimeMillis();
        compositeTask.setStatus(TaskStatus.RUNNING);

        for (TaskCommand cmd : commands) {
            boolean success = cmd.execute();
            if (success) {
                executedCommands.push(cmd);
            } else {
                compositeTask.setStatus(TaskStatus.FAILED);
                compositeTask.setErrorMessage("Composite step failed at task: " + cmd.getTask().getId());
                // Roll back already executed commands
                undo();
                totalExecutionTimeMs = System.currentTimeMillis() - start;
                return false;
            }
        }

        compositeTask.setStatus(TaskStatus.COMPLETED);
        totalExecutionTimeMs = System.currentTimeMillis() - start;
        return true;
    }

    @Override
    public void undo() {
        while (!executedCommands.isEmpty()) {
            TaskCommand cmd = executedCommands.pop();
            try {
                cmd.undo();
            } catch (Exception e) {
                System.err.println("Error rolling back command: " + e.getMessage());
            }
        }
        compositeTask.setStatus(TaskStatus.ROLLED_BACK);
    }

    @Override
    public Task getTask() {
        return compositeTask;
    }

    @Override
    public TaskStatus getStatus() {
        return compositeTask.getStatus();
    }

    @Override
    public long getExecutionTimeMs() {
        return totalExecutionTimeMs;
    }

    public List<TaskCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
