package agentflow.storage;

import agentflow.model.ExecutionLogEntry;
import agentflow.model.Task;
import agentflow.model.VerificationResult;
import agentflow.model.WorkflowContext;

import java.util.List;
import java.util.Map;

/**
 * Lightweight, zero-dependency JSON serializer.
 * Produces clean, indented JSON representations of workflow domain objects.
 */
public class JsonSerializer {

    public static String serializeWorkflow(WorkflowContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        appendField(sb, "workflowId", ctx.getWorkflowId(), 1);
        sb.append(",\n");
        appendField(sb, "goal", ctx.getGoal(), 1);
        sb.append(",\n");
        appendField(sb, "status", ctx.getStatus().name(), 1);
        sb.append(",\n");
        appendField(sb, "durationMs", ctx.getDuration().toMillis(), 1);
        sb.append(",\n");
        appendField(sb, "startTime", ctx.getStartTime() != null ? ctx.getStartTime().toString() : null, 1);
        sb.append(",\n");
        appendField(sb, "endTime", ctx.getEndTime() != null ? ctx.getEndTime().toString() : null, 1);
        sb.append(",\n");

        // Tasks array
        sb.append("  \"tasks\": [\n");
        List<Task> tasks = ctx.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("    {\n");
            appendField(sb, "id", t.getId(), 3);
            sb.append(",\n");
            appendField(sb, "title", t.getTitle(), 3);
            sb.append(",\n");
            appendField(sb, "description", t.getDescription(), 3);
            sb.append(",\n");
            appendField(sb, "tool", t.getRequiredTool(), 3);
            sb.append(",\n");
            appendField(sb, "status", t.getStatus().name(), 3);
            sb.append(",\n");
            appendField(sb, "retryCount", t.getRetryCount(), 3);
            sb.append(",\n");
            appendField(sb, "result", t.getResult() != null ? t.getResult().toString() : null, 3);
            sb.append(",\n");
            appendField(sb, "error", t.getErrorMessage(), 3);
            sb.append(",\n");
            sb.append("      \"dependencies\": [");
            for (int d = 0; d < t.getDependencies().size(); d++) {
                sb.append("\"").append(escape(t.getDependencies().get(d))).append("\"");
                if (d < t.getDependencies().size() - 1) sb.append(", ");
            }
            sb.append("]\n");
            sb.append("    }");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // Verification Object
        sb.append("  \"verification\": ");
        VerificationResult vr = ctx.getVerificationResult();
        if (vr == null) {
            sb.append("null,\n");
        } else {
            sb.append("{\n");
            appendField(sb, "passed", vr.passed(), 2);
            sb.append(",\n");
            appendField(sb, "score", vr.score(), 2);
            sb.append(",\n");
            appendField(sb, "summary", vr.summary(), 2);
            sb.append(",\n");
            sb.append("    \"checklist\": [");
            for (int c = 0; c < vr.checklist().size(); c++) {
                sb.append("\"").append(escape(vr.checklist().get(c))).append("\"");
                if (c < vr.checklist().size() - 1) sb.append(", ");
            }
            sb.append("],\n");
            sb.append("    \"issues\": [");
            for (int is = 0; is < vr.issues().size(); is++) {
                sb.append("\"").append(escape(vr.issues().get(is))).append("\"");
                if (is < vr.issues().size() - 1) sb.append(", ");
            }
            sb.append("]\n");
            sb.append("  },\n");
        }

        // Execution Logs
        sb.append("  \"logs\": [\n");
        List<ExecutionLogEntry> logs = ctx.getLogs();
        for (int l = 0; l < logs.size(); l++) {
            ExecutionLogEntry entry = logs.get(l);
            sb.append("    {\n");
            appendField(sb, "time", entry.formattedTime(), 3);
            sb.append(",\n");
            appendField(sb, "role", entry.role().name(), 3);
            sb.append(",\n");
            appendField(sb, "taskId", entry.taskId(), 3);
            sb.append(",\n");
            appendField(sb, "message", entry.message(), 3);
            sb.append("\n    }");
            if (l < logs.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

    private static void appendField(StringBuilder sb, String key, Object value, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        sb.append(indent).append("\"").append(key).append("\": ");
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else {
            sb.append("\"").append(escape(value.toString())).append("\"");
        }
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
