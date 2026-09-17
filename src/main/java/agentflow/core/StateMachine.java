package agentflow.core;

import agentflow.model.AgentRole;
import agentflow.model.ExecutionLogEntry;
import agentflow.model.Task;
import agentflow.model.TaskStatus;
import agentflow.model.WorkflowContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Enforces strict, deterministic state transitions for tasks and the overall workflow.
 * Notifies registered listeners upon valid state changes.
 */
public class StateMachine {
    private final List<WorkflowEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(WorkflowEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(WorkflowEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Attempts to transition the overall workflow to a new state.
     */
    public synchronized void transitionWorkflow(WorkflowContext context, TaskStatus targetStatus, AgentRole role, String reason) {
        TaskStatus current = context.getStatus();
        if (current == targetStatus) {
            return;
        }

        if (!current.canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                    String.format("Illegal workflow state transition: %s -> %s (Reason: %s)", current, targetStatus, reason)
            );
        }

        context.setStatus(targetStatus);
        ExecutionLogEntry entry = ExecutionLogEntry.transition(role, "WORKFLOW", current, targetStatus, reason);
        context.addLog(entry);

        for (WorkflowEventListener listener : listeners) {
            try {
                listener.onWorkflowStateChanged(context, current, targetStatus, reason);
            } catch (Exception e) {
                System.err.println("Listener error on workflow transition: " + e.getMessage());
            }
        }
    }

    /**
     * Attempts to transition an individual task to a new state.
     */
    public synchronized void transitionTask(WorkflowContext context, Task task, TaskStatus targetStatus, AgentRole role, String reason) {
        TaskStatus current = task.getStatus();
        if (current == targetStatus) {
            return;
        }

        if (!current.canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                    String.format("Illegal task [%s] state transition: %s -> %s (Reason: %s)",
                            task.getId(), current, targetStatus, reason)
            );
        }

        task.setStatus(targetStatus);
        ExecutionLogEntry entry = ExecutionLogEntry.transition(role, task.getId(), current, targetStatus, reason);
        context.addLog(entry);

        for (WorkflowEventListener listener : listeners) {
            try {
                listener.onTaskStateChanged(context, task, current, targetStatus, reason);
            } catch (Exception e) {
                System.err.println("Listener error on task transition: " + e.getMessage());
            }
        }
    }

    /**
     * Dispatches a general log/detail event to listeners.
     */
    public void notifyLog(WorkflowContext context, ExecutionLogEntry entry) {
        context.addLog(entry);
        for (WorkflowEventListener listener : listeners) {
            try {
                listener.onLogEntry(context, entry);
            } catch (Exception e) {
                System.err.println("Listener error on log notification: " + e.getMessage());
            }
        }
    }
}
