package agentflow.core;

import agentflow.agent.ExecutorAgent;
import agentflow.agent.PlannerAgent;
import agentflow.agent.VerifierAgent;
import agentflow.command.CommandHistory;
import agentflow.model.AgentResult;
import agentflow.model.AgentRole;
import agentflow.model.TaskStatus;
import agentflow.model.WorkflowContext;
import agentflow.storage.WorkflowStorageService;
import agentflow.strategy.StrategyRegistry;
import agentflow.tool.ToolRegistry;

import java.util.Objects;

/**
 * Orchestrates the end-to-end multi-agent pipeline:
 * Planner -> Executor -> Verifier (with adaptive replanning loops).
 */
public class WorkflowEngine {
    private final StateMachine stateMachine;
    private final ToolRegistry toolRegistry;
    private final StrategyRegistry strategyRegistry;
    private final CommandHistory commandHistory;
    private final PlannerAgent plannerAgent;
    private final ExecutorAgent executorAgent;
    private final VerifierAgent verifierAgent;
    private final WorkflowStorageService storageService;

    public WorkflowEngine(ToolRegistry toolRegistry, StrategyRegistry strategyRegistry,
                          WorkflowStorageService storageService) {
        this.stateMachine = new StateMachine();
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry cannot be null");
        this.strategyRegistry = Objects.requireNonNull(strategyRegistry, "strategyRegistry cannot be null");
        this.storageService = storageService != null ? storageService : new WorkflowStorageService();
        this.commandHistory = new CommandHistory();

        this.plannerAgent = new PlannerAgent();
        this.executorAgent = new ExecutorAgent(toolRegistry, strategyRegistry, commandHistory);
        this.verifierAgent = new VerifierAgent();
    }

    public WorkflowEngine() {
        this(new ToolRegistry(), new StrategyRegistry(), new WorkflowStorageService());
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    public StrategyRegistry getStrategyRegistry() {
        return strategyRegistry;
    }

    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    public WorkflowStorageService getStorageService() {
        return storageService;
    }

    public PlannerAgent getPlannerAgent() {
        return plannerAgent;
    }

    public ExecutorAgent getExecutorAgent() {
        return executorAgent;
    }

    public VerifierAgent getVerifierAgent() {
        return verifierAgent;
    }

    /**
     * Executes the complete agentic pipeline for a user-specified goal.
     *
     * @param goal high-level query or task prompt
     * @return populated WorkflowContext containing tasks, logs, results, and metrics
     */
    public WorkflowContext executeWorkflow(String goal) {
        WorkflowContext context = new WorkflowContext(goal);

        try {
            // 1. Transition workflow to RUNNING
            stateMachine.transitionWorkflow(context, TaskStatus.RUNNING, AgentRole.COORDINATOR,
                    "Workflow initialized for goal: " + goal);

            // 2. Stage 1: Planning
            AgentResult planResult = plannerAgent.process(context, stateMachine);
            if (!planResult.success()) {
                stateMachine.transitionWorkflow(context, TaskStatus.FAILED, AgentRole.COORDINATOR,
                        "Planning phase failed: " + planResult.message());
                persistSafely(context);
                return context;
            }

            // 3. Stage 2: Execution
            AgentResult execResult = executorAgent.process(context, stateMachine);

            // 4. Stage 3: Verification
            AgentResult verifyResult = verifierAgent.process(context, stateMachine);

            // 5. Check if replanning is needed (e.g. if verification failed or defects found)
            int replanAttempts = 0;
            int maxReplanAttempts = 1;

            while (!verifyResult.success() &&
                    context.getVerificationResult() != null &&
                    context.getVerificationResult().requiresReplanning() &&
                    replanAttempts < maxReplanAttempts) {

                replanAttempts++;
                stateMachine.transitionWorkflow(context, TaskStatus.RUNNING, AgentRole.COORDINATOR,
                        "Triggering adaptive replan iteration #" + replanAttempts);

                plannerAgent.replan(context, stateMachine, context.getVerificationResult().issues());
                executorAgent.process(context, stateMachine);
                verifyResult = verifierAgent.process(context, stateMachine);
            }

            // 6. Final workflow state transition
            if (verifyResult.success()) {
                stateMachine.transitionWorkflow(context, TaskStatus.COMPLETED, AgentRole.COORDINATOR,
                        "All pipeline stages completed and verified successfully.");
            } else {
                stateMachine.transitionWorkflow(context, TaskStatus.FAILED, AgentRole.COORDINATOR,
                        "Verification requirements not met after pipeline execution.");
            }

            verifierAgent.generateFinalSummary(context);

        } catch (Exception e) {
            stateMachine.transitionWorkflow(context, TaskStatus.FAILED, AgentRole.COORDINATOR,
                    "Uncaught exception in workflow pipeline: " + e.getMessage());
            verifierAgent.generateFinalSummary(context);
        } finally {
            persistSafely(context);
        }

        return context;
    }

    private void persistSafely(WorkflowContext context) {
        try {
            storageService.saveRun(context);
        } catch (Exception e) {
            System.err.println("Warning: Failed to persist workflow run: " + e.getMessage());
        }
    }
}
