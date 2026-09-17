package agentflow.model;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Shared memory bus and state context for a workflow execution.
 * Maintains tasks, execution logs, shared variables, and lifecycle timestamps.
 */
public class WorkflowContext {
    private final String workflowId;
    private final String goal;
    private TaskStatus status;
    private final List<Task> tasks;
    private final Map<String, Object> memory;
    private final List<ExecutionLogEntry> logs;
    private VerificationResult verificationResult;
    private String finalSummary;
    private final Instant startTime;
    private Instant endTime;

    public WorkflowContext(String goal) {
        this.workflowId = "wf-" + UUID.randomUUID().toString().substring(0, 8);
        this.goal = Objects.requireNonNull(goal, "goal cannot be null").trim();
        this.status = TaskStatus.PENDING;
        this.tasks = new CopyOnWriteArrayList<>();
        this.memory = new ConcurrentHashMap<>();
        this.logs = new CopyOnWriteArrayList<>();
        this.startTime = Instant.now();
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getGoal() {
        return goal;
    }

    public synchronized TaskStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(TaskStatus status) {
        this.status = status;
        if (status.isTerminal() && this.endTime == null) {
            this.endTime = Instant.now();
        }
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(Task task) {
        if (task != null) {
            this.tasks.add(task);
        }
    }

    public void setTasks(List<Task> newTasks) {
        this.tasks.clear();
        if (newTasks != null) {
            this.tasks.addAll(newTasks);
        }
    }

    public Optional<Task> getTaskById(String taskId) {
        return tasks.stream().filter(t -> t.getId().equals(taskId)).findFirst();
    }

    public Map<String, Object> getMemory() {
        return memory;
    }

    public void setMemoryValue(String key, Object value) {
        if (key != null && value != null) {
            this.memory.put(key, value);
        }
    }

    public Object getMemoryValue(String key) {
        return this.memory.get(key);
    }

    public List<ExecutionLogEntry> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public void addLog(ExecutionLogEntry entry) {
        if (entry != null) {
            this.logs.add(entry);
        }
    }

    public VerificationResult getVerificationResult() {
        return verificationResult;
    }

    public void setVerificationResult(VerificationResult verificationResult) {
        this.verificationResult = verificationResult;
    }

    public String getFinalSummary() {
        return finalSummary;
    }

    public void setFinalSummary(String finalSummary) {
        this.finalSummary = finalSummary;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        Instant finish = endTime != null ? endTime : Instant.now();
        return Duration.between(startTime, finish);
    }
}
