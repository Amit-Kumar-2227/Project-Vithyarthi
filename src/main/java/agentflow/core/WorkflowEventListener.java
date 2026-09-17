package agentflow.core;

import agentflow.model.ExecutionLogEntry;
import agentflow.model.Task;
import agentflow.model.TaskStatus;
import agentflow.model.WorkflowContext;

/**
 * Event listener interface for receiving real-time workflow lifecycle events.
 */
public interface WorkflowEventListener {

    void onWorkflowStateChanged(WorkflowContext context, TaskStatus from, TaskStatus to, String reason);

    void onTaskStateChanged(WorkflowContext context, Task task, TaskStatus from, TaskStatus to, String reason);

    void onLogEntry(WorkflowContext context, ExecutionLogEntry entry);
}
