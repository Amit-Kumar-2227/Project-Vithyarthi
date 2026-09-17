package agentflow.command;

import agentflow.model.Task;
import agentflow.model.TaskStatus;

/**
 * Command design pattern interface representing an executable task or action in the workflow.
 */
public interface TaskCommand {

    /**
     * Executes the encapsulated task.
     *
     * @return true if execution succeeded, false otherwise
     */
    boolean execute();

    /**
     * Reverses or compensates for the actions taken during execution (rollback).
     */
    void undo();

    /**
     * Returns the underlying task model.
     */
    Task getTask();

    /**
     * Returns the current status of the command.
     */
    TaskStatus getStatus();

    /**
     * Returns execution duration in milliseconds.
     */
    long getExecutionTimeMs();
}
