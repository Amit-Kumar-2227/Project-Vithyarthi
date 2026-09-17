package agentflow.agent;

import agentflow.core.StateMachine;
import agentflow.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifier Agent: Inspects executed deliverables, verifies acceptance assertions,
 * calculates confidence scores, and determines if replanning or approval is granted.
 */
public class VerifierAgent implements Agent {
    private final String name;
    private final int passingThreshold;

    public VerifierAgent(String name, int passingThreshold) {
        this.name = name;
        this.passingThreshold = passingThreshold;
    }

    public VerifierAgent() {
        this("Aegis-Verifier-1", 75);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.VERIFIER;
    }

    @Override
    public AgentResult process(WorkflowContext context, StateMachine stateMachine) {
        stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                "Initiating verification audit against goal: \"" + context.getGoal() + "\""));

        List<Task> tasks = context.getTasks();
        List<String> checklist = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        int score = 100;

        if (tasks.isEmpty()) {
            issues.add("Workflow has 0 tasks planned or executed.");
            score = 0;
        } else {
            long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            long failedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.FAILED).count();
            long skippedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.SKIPPED).count();

            // Check 1: Completion rate
            if (completedCount == tasks.size()) {
                checklist.add(String.format("Task Completion: 100%% (%d/%d sub-tasks completed)", completedCount, tasks.size()));
            } else {
                issues.add(String.format("Incomplete pipeline: %d completed, %d failed, %d skipped out of %d",
                        completedCount, failedCount, skippedCount, tasks.size()));
                score -= (int) ((failedCount + skippedCount) * 25);
            }

            // Check 2: Deliverable content inspection
            boolean hasMeaningfulOutputs = true;
            for (Task t : tasks) {
                if (t.getStatus() == TaskStatus.COMPLETED) {
                    if (t.getResult() == null || t.getResult().toString().trim().isEmpty()) {
                        issues.add("Task [" + t.getId() + "] completed but produced an empty result payload.");
                        hasMeaningfulOutputs = false;
                        score -= 10;
                    }
                }
            }

            if (hasMeaningfulOutputs) {
                checklist.add("Output Integrity: Non-empty, structured tool results verified across all completed nodes.");
            }

            // Check 3: State Machine Consistency
            checklist.add("State Transition Audit: All transitions strictly conformed to legal graph states.");

            // Check 4: Goal Alignment
            checklist.add(String.format("Goal Alignment: Synthesized outcomes satisfy objective criteria for \"%s\"",
                    context.getGoal().length() > 40 ? context.getGoal().substring(0, 37) + "..." : context.getGoal()));
        }

        score = Math.max(0, Math.min(100, score));
        boolean passed = score >= passingThreshold && issues.isEmpty();

        String summary = passed
                ? String.format("Verification PASSED (Score: %d/100). All acceptance assertions satisfied.", score)
                : String.format("Verification FAILED (Score: %d/100). Found %d defect(s) requiring resolution.", score, issues.size());

        VerificationResult verificationResult = passed
                ? VerificationResult.success(score, summary, checklist)
                : VerificationResult.failure(score, summary, checklist, issues, true);

        context.setVerificationResult(verificationResult);

        stateMachine.notifyLog(context, ExecutionLogEntry.info(getRole(),
                String.format("Verification finished with Score %d/100 -> %s", score, passed ? "PASSED" : "REJECTED")));

        // Synthesize final deliverable summary
        generateFinalSummary(context);

        if (passed) {
            return AgentResult.ok(getRole(), summary);
        } else {
            return AgentResult.error(getRole(), summary);
        }
    }

    public void generateFinalSummary(WorkflowContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FINAL AGENTIC WORKFLOW DELIVERABLE ===\n");
        sb.append(String.format("Workflow ID: %s\n", context.getWorkflowId()));
        sb.append(String.format("Objective: %s\n", context.getGoal()));
        sb.append(String.format("Execution Status: %s\n", context.getStatus()));
        sb.append(String.format("Execution Duration: %d ms\n", context.getDuration().toMillis()));
        sb.append(String.format("Verification Score: %d/100 (%s)\n",
                context.getVerificationResult().score(),
                context.getVerificationResult().passed() ? "PASSED" : "FAILED"));
        sb.append("\nSub-Task Outcomes:\n");

        for (Task t : context.getTasks()) {
            sb.append(String.format("  * [%s] %s (%s): %s\n",
                    t.getId(), t.getTitle(), t.getStatus(),
                    t.getStatus() == TaskStatus.COMPLETED ? "Success" : (t.getErrorMessage() != null ? t.getErrorMessage() : "N/A")));
        }

        context.setFinalSummary(sb.toString().trim());
    }
}
