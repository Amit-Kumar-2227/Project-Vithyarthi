package agentflow.agent;

import agentflow.core.StateMachine;
import agentflow.model.*;

import java.util.*;

/**
 * Planner Agent: Analyzes the high-level objective, deconstructs it into granular sub-tasks,
 * assigns required tools, establishes dependency edges, and generates compensation plans.
 */
public class PlannerAgent implements Agent {
    private final String name;

    public PlannerAgent(String name) {
        this.name = name;
    }

    public PlannerAgent() {
        this("Aegis-Planner-1");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.PLANNER;
    }

    @Override
    public AgentResult process(WorkflowContext context, StateMachine stateMachine) {
        stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                "Deconstructing workflow objective: \"" + context.getGoal() + "\""));

        List<Task> plan = createPlanForGoal(context.getGoal());
        context.setTasks(plan);

        for (Task t : plan) {
            stateMachine.notifyLog(context, ExecutionLogEntry.detail(getRole(), t.getId(),
                    String.format("Generated sub-task [%s]: %s (Tool: %s, Deps: %s)",
                            t.getId(), t.getTitle(), t.getRequiredTool(), t.getDependencies()),
                    t.getDescription()));
        }

        stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                String.format("Plan generated successfully with %d ordered sub-tasks.", plan.size())));

        return AgentResult.ok(getRole(), "Plan synthesized with " + plan.size() + " sub-tasks",
                Map.of("taskCount", plan.size()));
    }

    /**
     * Generates corrective or remediation sub-tasks when the VerifierAgent rejects intermediate output.
     */
    public AgentResult replan(WorkflowContext context, StateMachine stateMachine, List<String> issues) {
        stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                "Initiating adaptive replan based on Verifier feedback. Issues identified: " + issues.size()));

        List<Task> existing = new ArrayList<>(context.getTasks());
        int patchIndex = existing.size() + 1;

        String patchTaskId = "task-" + patchIndex;
        Task remediationTask = new Task(
                patchTaskId,
                "Remediation & Compliance Patch",
                "Apply corrective adjustments addressing: " + String.join("; ", issues),
                "data_processor",
                Map.of("operation", "transform", "input", "Addressing verified issues: " + String.join(", ", issues))
        );

        if (!existing.isEmpty()) {
            remediationTask.addDependency(existing.get(existing.size() - 1).getId());
        }

        context.addTask(remediationTask);
        stateMachine.notifyLog(context, ExecutionLogEntry.detail(getRole(), patchTaskId,
                "Added remediation subtask [" + patchTaskId + "] to resolve verification flaws.",
                remediationTask.getDescription()));

        return AgentResult.ok(getRole(), "Adaptive replanning completed. Added task " + patchTaskId);
    }

    private List<Task> createPlanForGoal(String goal) {
        String lower = goal.toLowerCase();

        if (lower.contains("code") || lower.contains("refactor") || lower.contains("audit") || lower.contains("vulnerability")) {
            return buildCodeRefactorPlan(goal);
        } else if (lower.contains("market") || lower.contains("competitor") || lower.contains("research")) {
            return buildMarketResearchPlan(goal);
        } else if (lower.contains("incident") || lower.contains("outage") || lower.contains("triage") || lower.contains("alert")) {
            return buildIncidentTriagePlan(goal);
        } else if (lower.contains("finance") || lower.contains("earnings") || lower.contains("revenue") || lower.contains("stock")) {
            return buildFinancialAnalysisPlan(goal);
        } else {
            return buildGenericAdaptivePlan(goal);
        }
    }

    private List<Task> buildCodeRefactorPlan(String goal) {
        List<Task> tasks = new ArrayList<>();

        Task t1 = new Task("task-1", "Static Code & Security Inspection",
                "Scan repository for anti-patterns, security vulnerabilities, and code health.",
                "code_analysis",
                Map.of("codeSnippet", "class ServiceHandler { void process() { Thread.sleep(100); } }",
                        "analysisType", "security"));

        Task t2 = new Task("task-2", "Research Refactoring Patterns",
                "Query best practices for modern concurrency and clean architecture.",
                "web_search",
                Map.of("query", "modern java concurrency patterns best practices", "maxResults", 2));
        t2.addDependency("task-1");

        Task t3 = new Task("task-3", "Generate Refactored Source",
                "Persist refactored implementation into workspace sandbox.",
                "file_system",
                Map.of("action", "write",
                        "path", "src/RefactoredHandler.java",
                        "content", "// Refactored using modern Java Records and State Machine\npublic record RefactoredHandler(String id) {}"));
        t3.addDependency("task-2");

        Task t4 = new Task("task-4", "Verify Code Quality & Pattern Compliance",
                "Run final lint and code health checks against the refactored output.",
                "code_analysis",
                Map.of("codeSnippet", "public record RefactoredHandler(String id) { public RefactoredHandler { Objects.requireNonNull(id); } }",
                        "analysisType", "quality"));
        t4.addDependency("task-3");

        Task t5 = new Task("task-5", "Dispatch Deployment Notification",
                "Notify engineering teams of successful code refactoring.",
                "notification",
                Map.of("channel", "devops-alerts", "message", "Code refactoring pipeline verified and staged.", "priority", "INFO"));
        t5.addDependency("task-4");

        tasks.addAll(List.of(t1, t2, t3, t4, t5));
        return tasks;
    }

    private List<Task> buildMarketResearchPlan(String goal) {
        List<Task> tasks = new ArrayList<>();

        Task t1 = new Task("task-1", "Competitive Intelligence Search",
                "Gather latest industry intelligence and competitor landscape data.",
                "web_search",
                Map.of("query", "market trend competitor analysis autonomous agent systems", "maxResults", 3));

        Task t2 = new Task("task-2", "Quantitative Data Synthesis",
                "Aggregate market metrics, adoption percentages, and growth statistics.",
                "data_processor",
                Map.of("operation", "aggregate", "input", "Adoption stats: 45.5, 78.2, 92.4, 110.0, 145.0"));
        t2.addDependency("task-1");

        Task t3 = new Task("task-3", "Compile Intelligence Dossier",
                "Write compiled market research dossier into workspace file.",
                "file_system",
                Map.of("action", "write",
                        "path", "reports/market_intelligence_2026.md",
                        "content", "# Executive Market Dossier\nSynthesized multi-agent benchmark and adoption metrics."));
        t3.addDependency("task-2");

        Task t4 = new Task("task-4", "Publish Executive Summary Alert",
                "Dispatch executive summary notice to stakeholders.",
                "notification",
                Map.of("channel", "executive-briefs", "message", "Market intelligence report compiled and verified.", "priority", "INFO"));
        t4.addDependency("task-3");

        tasks.addAll(List.of(t1, t2, t3, t4));
        return tasks;
    }

    private List<Task> buildIncidentTriagePlan(String goal) {
        List<Task> tasks = new ArrayList<>();

        Task t1 = new Task("task-1", "Query Diagnostic SRE Knowledge Base",
                "Retrieve known remediation runbooks and incident signatures.",
                "web_search",
                Map.of("query", "incident triage distributed tracing cloud outage", "maxResults", 2));

        Task t2 = new Task("task-2", "Analyze Telemetry & Latency Metrics",
                "Compute latency percentiles, error rates, and anomalous spikes.",
                "data_processor",
                Map.of("operation", "calculate", "input", "Response times ms: 120, 145, 980, 2400, 3100, 450, 130"));
        t2.addDependency("task-1");

        Task t3 = new Task("task-3", "Write Incident Postmortem Draft",
                "Save preliminary triage report and remediation action items.",
                "file_system",
                Map.of("action", "write",
                        "path", "incident_logs/triage_postmortem.txt",
                        "content", "Incident Severity: High\nRoot Cause: Memory leak in connection pool.\nRemediation: Pool recycled."));
        t3.addDependency("task-2");

        Task t4 = new Task("task-4", "Dispatch On-Call SRE Alert",
                "Send incident resolution status to on-call channel.",
                "notification",
                Map.of("channel", "sre-pagerduty", "message", "Incident triage complete. Remediation executed and verified.", "priority", "CRITICAL"));
        t4.addDependency("task-3");

        tasks.addAll(List.of(t1, t2, t3, t4));
        return tasks;
    }

    private List<Task> buildFinancialAnalysisPlan(String goal) {
        List<Task> tasks = new ArrayList<>();

        Task t1 = new Task("task-1", "Retrieve Earnings Data & SEC Disclosures",
                "Query financial publications and earnings disclosures.",
                "web_search",
                Map.of("query", "quarterly earnings revenue growth operating margin guidance", "maxResults", 2));

        Task t2 = new Task("task-2", "Compute Financial Ratios & Growth Margins",
                "Calculate revenue sum, average quarter growth, and variance.",
                "data_processor",
                Map.of("operation", "aggregate", "input", "Quarterly revenues ($M): 1420.5, 1580.0, 1750.2, 1920.8"));
        t2.addDependency("task-1");

        Task t3 = new Task("task-3", "Generate Financial Audit Spreadsheet/File",
                "Persist financial audit record into sandbox.",
                "file_system",
                Map.of("action", "write",
                        "path", "finance/quarterly_audit.csv",
                        "content", "Quarter,Revenue,Growth\nQ1,1420.5,-\nQ2,1580.0,11.2%\nQ3,1750.2,10.7%\nQ4,1920.8,9.7%"));
        t3.addDependency("task-2");

        Task t4 = new Task("task-4", "Notify CFO & Finance Stakeholders",
                "Broadcast financial summary alert.",
                "notification",
                Map.of("channel", "finance-exec", "message", "Quarterly financial model verified and archived.", "priority", "INFO"));
        t4.addDependency("task-3");

        tasks.addAll(List.of(t1, t2, t3, t4));
        return tasks;
    }

    private List<Task> buildGenericAdaptivePlan(String goal) {
        List<Task> tasks = new ArrayList<>();

        Task t1 = new Task("task-1", "Contextual Research & Ingestion",
                "Gather external context and parameters for: " + goal,
                "web_search",
                Map.of("query", goal, "maxResults", 2));

        Task t2 = new Task("task-2", "Data Synthesis & Transformation",
                "Process acquired information and structure outputs.",
                "data_processor",
                Map.of("operation", "transform", "input", "Synthesizing parameters for objective: " + goal));
        t2.addDependency("task-1");

        Task t3 = new Task("task-3", "Persist Deliverables",
                "Record execution output into workspace storage.",
                "file_system",
                Map.of("action", "write",
                        "path", "output/deliverable.txt",
                        "content", "Deliverable for goal: " + goal + "\nGenerated on: " + new Date()));
        t3.addDependency("task-2");

        Task t4 = new Task("task-4", "Completion Broadcast",
                "Dispatch completion notification.",
                "notification",
                Map.of("channel", "system-log", "message", "Task workflow executed for: " + goal, "priority", "INFO"));
        t4.addDependency("task-3");

        tasks.addAll(List.of(t1, t2, t3, t4));
        return tasks;
    }
}
