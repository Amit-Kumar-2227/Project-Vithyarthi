package agentflow.model;

import java.util.EnumSet;

/**
 * Represents the lifecycle state of a task or overall workflow.
 * Encapsulates legal state transitions to enforce deterministic state flow.
 */
public enum TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED,
    ROLLED_BACK;

    /**
     * Checks if transitioning from this status to the target status is permitted.
     *
     * @param target the desired state to transition to
     * @return true if the transition is legal, false otherwise
     */
    public boolean canTransitionTo(TaskStatus target) {
        if (this == target) {
            return true; // No-op transition
        }

        return switch (this) {
            case PENDING -> EnumSet.of(RUNNING, SKIPPED, FAILED).contains(target);
            case RUNNING -> EnumSet.of(COMPLETED, FAILED, ROLLED_BACK).contains(target);
            case FAILED -> EnumSet.of(PENDING, RUNNING, ROLLED_BACK).contains(target); // Allow retry/replan
            case COMPLETED -> EnumSet.of(RUNNING, ROLLED_BACK).contains(target); // Allow compensation
            case ROLLED_BACK -> EnumSet.of(PENDING, RUNNING).contains(target);
            case SKIPPED -> EnumSet.of(PENDING).contains(target);
        };
    }

    /**
     * Returns true if this state is a terminal state under normal circumstances.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == SKIPPED || this == ROLLED_BACK;
    }
}
