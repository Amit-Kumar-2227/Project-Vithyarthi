package agentflow;

import agentflow.agent.PlannerAgent;
import agentflow.command.CommandHistory;
import agentflow.command.CompositeTaskCommand;
import agentflow.command.ToolCallCommand;
import agentflow.core.StateMachine;
import agentflow.core.WorkflowEngine;
import agentflow.model.AgentResult;
import agentflow.model.AgentRole;
import agentflow.model.Task;
import agentflow.model.TaskStatus;
import agentflow.model.WorkflowContext;
import agentflow.storage.JsonSerializer;
import agentflow.storage.WorkflowStorageService;
import agentflow.strategy.DeterministicExecutionStrategy;
import agentflow.strategy.FaultTolerantFallbackStrategy;
import agentflow.strategy.SimulatedApiStrategy;
import agentflow.tool.FileSystemTool;
import agentflow.tool.Tool;
import agentflow.tool.ToolRegistry;
import agentflow.tool.ToolResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Automated verification test suite for Modular Agentic Workflow System.
 * Tests state transitions, design patterns (Command, Strategy), agents, and persistence.
 */
public class WorkflowVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING AGENTIC WORKFLOW AUTOMATED TEST SUITE  ");
        System.out.println("=================================================");

        testStateMachineLegalTransitions();
        testStateMachineIllegalTransition();
        testPlannerAgentDecomposition();
        testCommandPatternExecutionAndUndo();
        testCompositeCommandRollback();
        testDynamicStrategies();
        testVerifierScoring();
        testJsonSerialization();
        testEndToEndWorkflowRuns();

        System.out.println("=================================================");
        System.out.printf("  TEST RESULTS: %d/%d PASSED%n", testsPassed, testsRun);
        System.out.println("=================================================");

        if (testsPassed != testsRun) {
            System.err.println("Some tests failed!");
            System.exit(1);
        } else {
            System.out.println("All verification tests passed successfully!");
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %s%n", testName);
        } else {
            System.err.printf("  [FAIL] %s: %s%n", testName, message);
        }
    }

    private static void testStateMachineLegalTransitions() {
        StateMachine sm = new StateMachine();
        WorkflowContext ctx = new WorkflowContext("Test Goal");
        Task task = new Task("t1", "Title", "Desc", "tool");

        sm.transitionWorkflow(ctx, TaskStatus.RUNNING, AgentRole.COORDINATOR, "Start");
        assertTrue("Workflow transition PENDING -> RUNNING", ctx.getStatus() == TaskStatus.RUNNING, "Expected RUNNING");

        sm.transitionTask(ctx, task, TaskStatus.RUNNING, AgentRole.EXECUTOR, "Start Task");
        assertTrue("Task transition PENDING -> RUNNING", task.getStatus() == TaskStatus.RUNNING, "Expected RUNNING");

        sm.transitionTask(ctx, task, TaskStatus.COMPLETED, AgentRole.EXECUTOR, "Finish Task");
        assertTrue("Task transition RUNNING -> COMPLETED", task.getStatus() == TaskStatus.COMPLETED, "Expected COMPLETED");
    }

    private static void testStateMachineIllegalTransition() {
        StateMachine sm = new StateMachine();
        WorkflowContext ctx = new WorkflowContext("Test Illegal");
        boolean caught = false;
        try {
            // Illegal transition: PENDING directly to COMPLETED without RUNNING
            sm.transitionWorkflow(ctx, TaskStatus.COMPLETED, AgentRole.COORDINATOR, "Illegal Jump");
        } catch (IllegalStateException e) {
            caught = true;
        }
        assertTrue("Illegal state transition throws IllegalStateException", caught, "Expected exception");
    }

    private static void testPlannerAgentDecomposition() {
        PlannerAgent planner = new PlannerAgent();
        StateMachine sm = new StateMachine();
        WorkflowContext ctx = new WorkflowContext("Audit repository for security vulnerabilities and refactor concurrency handlers");

        AgentResult result = planner.process(ctx, sm);
        assertTrue("Planner process success", result.success(), "Planner should succeed");
        assertTrue("Planner generated subtasks", ctx.getTasks().size() >= 4, "Expected >= 4 subtasks");
        assertTrue("Planner created dependency links", !ctx.getTasks().get(1).getDependencies().isEmpty(), "Expected dependencies");
    }

    private static void testCommandPatternExecutionAndUndo() {
        ToolRegistry registry = new ToolRegistry();
        StateMachine sm = new StateMachine();
        WorkflowContext ctx = new WorkflowContext("Test Command");
        CommandHistory history = new CommandHistory();

        Path testDir = Path.of("./test_sandbox");
        FileSystemTool fsTool = new FileSystemTool(testDir);
        Task task = new Task("test-t1", "Write File", "Writes test file", "file_system",
                Map.of("action", "write", "path", "temp_test.txt", "content", "Hello Agentic World"));

        ToolCallCommand cmd = new ToolCallCommand(task, fsTool, new DeterministicExecutionStrategy(0), ctx, sm);
        history.record(cmd);

        boolean executed = cmd.execute();
        assertTrue("ToolCallCommand execution", executed && task.getStatus() == TaskStatus.COMPLETED, "Execution failed");
        assertTrue("File created on disk", Files.exists(testDir.resolve("temp_test.txt")), "File should exist");

        // Undo (Compensation)
        cmd.undo();
        assertTrue("Task rolled back status", task.getStatus() == TaskStatus.ROLLED_BACK, "Expected ROLLED_BACK");
        assertTrue("File removed on rollback", !Files.exists(testDir.resolve("temp_test.txt")), "File should be removed");

        try {
            Files.deleteIfExists(testDir);
        } catch (Exception ignored) {}
    }

    private static void testCompositeCommandRollback() {
        StateMachine sm = new StateMachine();
        WorkflowContext ctx = new WorkflowContext("Test Composite");

        Path testDir = Path.of("./test_composite_sandbox");
        FileSystemTool fsTool = new FileSystemTool(testDir);

        Task step1 = new Task("s1", "Write file 1", "desc", "file_system",
                Map.of("action", "write", "path", "s1.txt", "content", "Data 1"));
        Task step2 = new Task("s2", "Failing step", "desc", "file_system",
                Map.of("action", "invalid_action", "path", "s2.txt"));

        ToolCallCommand cmd1 = new ToolCallCommand(step1, fsTool, new DeterministicExecutionStrategy(0), ctx, sm);
        ToolCallCommand cmd2 = new ToolCallCommand(step2, fsTool, new DeterministicExecutionStrategy(0), ctx, sm);

        Task compositeTask = new Task("comp", "Composite Workflow", "desc", "none");
        CompositeTaskCommand composite = new CompositeTaskCommand(compositeTask, List.of(cmd1, cmd2));

        boolean success = composite.execute();
        assertTrue("Composite command fails when step 2 fails", !success, "Composite should fail");
        assertTrue("Composite task status is ROLLED_BACK", compositeTask.getStatus() == TaskStatus.ROLLED_BACK, "Expected ROLLED_BACK");
        assertTrue("Step 1 rolled back and file cleaned up", !Files.exists(testDir.resolve("s1.txt")), "File should be rolled back");

        try {
            Files.deleteIfExists(testDir);
        } catch (Exception ignored) {}
    }

    private static void testDynamicStrategies() {
        ToolRegistry registry = new ToolRegistry();
        WorkflowContext ctx = new WorkflowContext("Test Strategies");
        Tool tool = registry.getTool("data_processor").orElseThrow();

        // 1. Deterministic
        DeterministicExecutionStrategy det = new DeterministicExecutionStrategy(0);
        ToolResult r1 = det.execute(tool, Map.of("operation", "calculate", "input", "10, 20, 30"), ctx);
        assertTrue("Deterministic strategy execution", r1.success(), "Should succeed");

        // 2. Simulated API
        SimulatedApiStrategy api = new SimulatedApiStrategy(0);
        ToolResult r2 = api.execute(tool, Map.of("operation", "transform", "input", "test data payload"), ctx);
        assertTrue("Simulated API strategy execution", r2.success() && r2.output().contains("https://api.agentic.internal"), "API output missing headers");

        // 3. Fault tolerant
        FaultTolerantFallbackStrategy fallback = new FaultTolerantFallbackStrategy(2, 10);
        ToolResult r3 = fallback.execute(tool, Map.of("operation", "calculate", "input", "100, 200"), ctx);
        assertTrue("Fault-tolerant strategy execution", r3.success(), "Should succeed");
    }

    private static void testVerifierScoring() {
        WorkflowEngine engine = new WorkflowEngine();
        WorkflowContext ctx = engine.executeWorkflow("Research market trends and competitor data for AI agents in 2026");

        assertTrue("Workflow finished COMPLETED", ctx.getStatus() == TaskStatus.COMPLETED, "Status should be COMPLETED");
        assertTrue("Verification result present", ctx.getVerificationResult() != null, "VerificationResult cannot be null");
        assertTrue("Verifier score >= 75", ctx.getVerificationResult().score() >= 75, "Score should be >= 75");
        assertTrue("Verifier checklist has items", !ctx.getVerificationResult().checklist().isEmpty(), "Checklist empty");
    }

    private static void testJsonSerialization() {
        WorkflowContext ctx = new WorkflowContext("Serialization Test");
        ctx.setStatus(TaskStatus.COMPLETED);
        Task t = new Task("t1", "Task 1", "Desc", "web_search");
        t.setStatus(TaskStatus.COMPLETED);
        t.setResult("Found 3 search citations");
        ctx.addTask(t);

        String json = JsonSerializer.serializeWorkflow(ctx);
        assertTrue("JSON contains workflowId", json.contains("\"workflowId\": \"wf-"), "JSON missing workflowId");
        assertTrue("JSON contains task id", json.contains("\"id\": \"t1\""), "JSON missing task id");
        assertTrue("JSON contains status", json.contains("\"status\": \"COMPLETED\""), "JSON missing status");
    }

    private static void testEndToEndWorkflowRuns() {
        WorkflowEngine engine = new WorkflowEngine();

        // Run scenario 1: Incident triage
        WorkflowContext ctx1 = engine.executeWorkflow("Perform cloud incident triage for latency alert");
        assertTrue("Incident triage completed", ctx1.getStatus() == TaskStatus.COMPLETED, "Expected COMPLETED");

        // Run scenario 2: Financial analysis
        WorkflowContext ctx2 = engine.executeWorkflow("Aggregate quarterly corporate earnings and calculate growth");
        assertTrue("Financial analysis completed", ctx2.getStatus() == TaskStatus.COMPLETED, "Expected COMPLETED");

        // Check that storage saved runs
        List<Path> runs = engine.getStorageService().listSavedRuns();
        assertTrue("Workflow runs saved to disk", !runs.isEmpty(), "Expected at least 1 saved run file");
    }
}
