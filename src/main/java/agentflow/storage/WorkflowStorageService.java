package agentflow.storage;

import agentflow.model.ExecutionLogEntry;
import agentflow.model.Task;
import agentflow.model.WorkflowContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Storage service for persisting workflow execution runs as JSON and human-readable Markdown.
 */
public class WorkflowStorageService {
    private final Path logsDirectory;
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

    public WorkflowStorageService(Path logsDirectory) {
        this.logsDirectory = logsDirectory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.logsDirectory);
        } catch (IOException e) {
            System.err.println("Warning: Could not create logs directory: " + e.getMessage());
        }
    }

    public WorkflowStorageService() {
        this(Path.of("./workflow_logs"));
    }

    public Path getLogsDirectory() {
        return logsDirectory;
    }

    /**
     * Persists workflow execution to disk in both JSON and Markdown formats.
     */
    public void saveRun(WorkflowContext context) throws IOException {
        String timestamp = FILE_DATE_FORMAT.format(context.getStartTime());
        String baseName = String.format("run_%s_%s", timestamp, context.getWorkflowId());

        // 1. Write JSON file
        Path jsonPath = logsDirectory.resolve(baseName + ".json");
        String jsonContent = JsonSerializer.serializeWorkflow(context);
        Files.writeString(jsonPath, jsonContent);

        // 2. Write Markdown timeline file
        Path mdPath = logsDirectory.resolve(baseName + ".md");
        String mdContent = generateMarkdownTimeline(context);
        Files.writeString(mdPath, mdContent);
    }

    /**
     * Lists all saved workflow runs (JSON files) ordered by newest first.
     */
    public List<Path> listSavedRuns() {
        if (!Files.exists(logsDirectory)) {
            return Collections.emptyList();
        }
        try (var stream = Files.list(logsDirectory)) {
            return stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted((a, b) -> b.getFileName().compareTo(a.getFileName()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public String readRun(Path path) throws IOException {
        return Files.readString(path);
    }

    private String generateMarkdownTimeline(WorkflowContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Agentic Workflow Execution Report\n\n");
        sb.append("- **Workflow ID**: `").append(ctx.getWorkflowId()).append("`\n");
        sb.append("- **Objective**: ").append(ctx.getGoal()).append("\n");
        sb.append("- **Final Status**: `").append(ctx.getStatus()).append("`\n");
        sb.append("- **Duration**: ").append(ctx.getDuration().toMillis()).append(" ms\n");
        if (ctx.getVerificationResult() != null) {
            sb.append("- **Verification Score**: ").append(ctx.getVerificationResult().score()).append("/100 (")
                    .append(ctx.getVerificationResult().passed() ? "PASSED" : "FAILED").append(")\n");
        }
        sb.append("\n---\n\n");

        sb.append("## Task Pipeline Summary\n\n");
        sb.append("| Task ID | Title | Required Tool | Status | Duration/Result |\n");
        sb.append("|---|---|---|---|---|\n");
        for (Task t : ctx.getTasks()) {
            sb.append(String.format("| `%s` | %s | `%s` | **%s** | %s |\n",
                    t.getId(), t.getTitle(), t.getRequiredTool(), t.getStatus(),
                    t.getStatus() == agentflow.model.TaskStatus.COMPLETED ? "Success" : "Error/Pending"));
        }
        sb.append("\n---\n\n");

        sb.append("## Chronological Agent Execution Log\n\n");
        for (ExecutionLogEntry entry : ctx.getLogs()) {
            sb.append(String.format("- `[%s]` **[%s]** ", entry.formattedTime(), entry.role()));
            if (entry.taskId() != null) {
                sb.append("(`").append(entry.taskId()).append("`) ");
            }
            sb.append(entry.message()).append("\n");
            if (entry.details() != null && !entry.details().isBlank()) {
                sb.append("  > ").append(entry.details().replace("\n", "\n  > ")).append("\n");
            }
        }

        if (ctx.getVerificationResult() != null) {
            sb.append("\n---\n\n## Verification Audit Checklist\n\n");
            for (String item : ctx.getVerificationResult().checklist()) {
                sb.append("- [x] ").append(item).append("\n");
            }
            if (!ctx.getVerificationResult().issues().isEmpty()) {
                sb.append("\n### Issues Identified\n\n");
                for (String issue : ctx.getVerificationResult().issues()) {
                    sb.append("- [ ] ⚠️ ").append(issue).append("\n");
                }
            }
        }

        sb.append("\n---\n*Report generated automatically by Modular Agentic Workflow Engine.*\n");
        return sb.toString();
    }
}
