package agentflow.model;

import java.time.Instant;
import java.util.*;

/**
 * Represents a granular task or sub-task in the agentic workflow pipeline.
 */
public class Task {
    private final String id;
    private final String title;
    private final String description;
    private final String requiredTool;
    private final Map<String, Object> parameters;
    private final List<String> dependencies;
    private TaskStatus status;
    private Object result;
    private String errorMessage;
    private int retryCount;
    private final Instant createdAt;
    private Instant completedAt;

    public Task(String id, String title, String description, String requiredTool, Map<String, Object> parameters) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.description = description != null ? description : "";
        this.requiredTool = requiredTool;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.dependencies = new ArrayList<>();
        this.status = TaskStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public Task(String id, String title, String description, String requiredTool) {
        this(id, title, description, requiredTool, Collections.emptyMap());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiredTool() {
        return requiredTool;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public void addDependency(String taskId) {
        if (taskId != null && !this.dependencies.contains(taskId)) {
            this.dependencies.add(taskId);
        }
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status.isTerminal() && this.completedAt == null) {
            this.completedAt = Instant.now();
        }
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    @Override
    public String toString() {
        return String.format("Task[id=%s, title='%s', tool='%s', status=%s]", id, title, requiredTool, status);
    }
}
