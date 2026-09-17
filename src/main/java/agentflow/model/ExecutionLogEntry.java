package agentflow.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * An immutable log entry recording real-time workflow events, state transitions, and agent decisions.
 */
public record ExecutionLogEntry(
        Instant timestamp,
        AgentRole role,
        String taskId,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        String message,
        String details
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    public static ExecutionLogEntry info(AgentRole role, String message) {
        return new ExecutionLogEntry(Instant.now(), role, null, null, null, message, null);
    }

    public static ExecutionLogEntry transition(AgentRole role, String taskId, TaskStatus from, TaskStatus to, String message) {
        return new ExecutionLogEntry(Instant.now(), role, taskId, from, to, message, null);
    }

    public static ExecutionLogEntry detail(AgentRole role, String taskId, String message, String details) {
        return new ExecutionLogEntry(Instant.now(), role, taskId, null, null, message, details);
    }

    public String formattedTime() {
        return FORMATTER.format(timestamp);
    }
}
