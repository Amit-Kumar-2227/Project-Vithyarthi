package agentflow.ui;

import agentflow.core.StateMachine;
import agentflow.core.WorkflowEngine;
import agentflow.core.WorkflowEventListener;
import agentflow.model.*;
import agentflow.strategy.ExecutionStrategy;
import agentflow.tool.Tool;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive Command-Line Interface menu.
 * Provides user interaction, real-time live event streaming, demo scenario launchers,
 * strategy switching, and history log inspection.
 */
public class CliMenu {
    private final WorkflowEngine engine;
    private final Scanner scanner;
    private boolean running = true;

    public CliMenu(WorkflowEngine engine) {
        this.engine = engine;
        this.scanner = new Scanner(System.in);
        attachRealtimeEventListener();
    }

    private void attachRealtimeEventListener() {
        engine.getStateMachine().addListener(new WorkflowEventListener() {
            @Override
            public void onWorkflowStateChanged(WorkflowContext context, TaskStatus from, TaskStatus to, String reason) {
                System.out.printf(" %s %s Workflow State: %s -> %s %s%n",
                        AnsiConsole.agentBadge(AgentRole.COORDINATOR),
                        AnsiConsole.bold(from != null ? from.name() : "INIT"),
                        AnsiConsole.bold("->"),
                        AnsiConsole.statusBadge(to),
                        AnsiConsole.dim("(Reason: " + reason + ")"));
            }

            @Override
            public void onTaskStateChanged(WorkflowContext context, Task task, TaskStatus from, TaskStatus to, String reason) {
                System.out.printf(" %s %s Node [%s] (%s): %s -> %s %s%n",
                        AnsiConsole.agentBadge(AgentRole.EXECUTOR),
                        AnsiConsole.cyan("[" + task.getId() + "]"),
                        task.getTitle(),
                        task.getRequiredTool(),
                        AnsiConsole.statusBadge(from),
                        AnsiConsole.statusBadge(to),
                        AnsiConsole.dim("[" + reason + "]"));
            }

            @Override
            public void onLogEntry(WorkflowContext context, ExecutionLogEntry entry) {
                if (entry.details() != null && !entry.details().isBlank()) {
                    System.out.printf("   %s %s%n",
                            AnsiConsole.yellow("↳ [Output Payload]:"),
                            indentBlock(entry.details(), "     "));
                }
            }
        });
    }

    public void start() {
        AnsiConsole.printBanner();

        while (running) {
            printMainMenu();
            String input = readInput("Select option (1-7): ");

            switch (input) {
                case "1" -> handleCustomTask();
                case "2" -> handleDemoScenarios();
                case "3" -> handleViewHistory();
                case "4" -> handleInspectComponents();
                case "5" -> handleConfigureStrategy();
                case "6" -> handleExplainArchitecture();
                case "7", "exit", "quit", "q" -> {
                    System.out.println(AnsiConsole.cyan("\nExiting Modular Agentic Workflow System. Goodbye!"));
                    running = false;
                }
                default -> System.out.println(AnsiConsole.red("Invalid option. Please enter a number between 1 and 7."));
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println(AnsiConsole.cyan("========================= [ MAIN CONTROL MENU ] ========================="));
        System.out.println("  1.  Enter Custom Task / Query");
        System.out.println("  2.  Run Pre-configured Demo Scenarios");
        System.out.println("  3.  View Workflow Execution History & Logs (JSON/Markdown)");
        System.out.println("  4.  Inspect Agents, Registered Tools & Dynamic Strategies");
        System.out.println("  5.  Configure Dynamic Execution Strategy");
        System.out.println("  6.  System Architecture & Design Patterns Overview");
        System.out.println("  7.  Exit");
        System.out.println(AnsiConsole.cyan("========================================================================="));
        System.out.printf("  Active Execution Strategy: %s%n",
                AnsiConsole.green(engine.getStrategyRegistry().getActiveStrategy().getName()));
    }

    private void handleCustomTask() {
        AnsiConsole.printHeader("CUSTOM TASK INPUT");
        System.out.println("Enter a complex goal or query for the multi-agent pipeline.");
        System.out.println("Examples:");
        System.out.println("  - Refactor authentication service with security audit and best practices");
        System.out.println("  - Research market trends for autonomous AI agents in 2026");
        System.out.println("  - Incident triage for latency spike in payment gateway");
        System.out.println();

        String goal = readInput("Enter Goal/Query: ");
        if (goal.isBlank()) {
            System.out.println(AnsiConsole.yellow("Task input cancelled or empty. Returning to menu."));
            return;
        }

        executeAndDisplay(goal);
    }

    private void handleDemoScenarios() {
        AnsiConsole.printHeader("PRE-CONFIGURED DEMO SCENARIOS");
        System.out.println("  [1] Automated Code Audit & Security Refactoring");
        System.out.println("  [2] Enterprise Market Intelligence & Competitor Report");
        System.out.println("  [3] Cloud Infrastructure SRE Incident Triage & Auto-Remediation");
        System.out.println("  [4] Quarterly Financial Earnings Aggregation & Ratio Modeling");
        System.out.println("  [B] Back to Main Menu");
        System.out.println();

        String choice = readInput("Select Scenario (1-4): ");
        String goal = switch (choice) {
            case "1" -> "Audit repository for security vulnerabilities and refactor concurrency handlers";
            case "2" -> "Conduct enterprise market research on AI agent orchestration benchmarks for 2026";
            case "3" -> "Perform automated cloud SRE incident triage and diagnose high-latency database connection spikes";
            case "4" -> "Aggregate quarterly corporate earnings, calculate financial growth margins, and notify CFO";
            default -> null;
        };

        if (goal == null) {
            System.out.println(AnsiConsole.yellow("Returning to menu."));
            return;
        }

        executeAndDisplay(goal);
    }

    private void executeAndDisplay(String goal) {
        AnsiConsole.printHeader("DISPATCHING MULTI-AGENT WORKFLOW");
        System.out.printf("Goal: %s%n", AnsiConsole.bold(goal));
        System.out.printf("Active Strategy: %s%n",
                AnsiConsole.cyan(engine.getStrategyRegistry().getActiveStrategy().getName()));
        System.out.println(AnsiConsole.dim("Streaming live agent decisions and state transitions...\n"));

        long startTime = System.currentTimeMillis();
        WorkflowContext resultCtx = engine.executeWorkflow(goal);
        long elapsed = System.currentTimeMillis() - startTime;

        // Print final verdict banner
        System.out.println();
        AnsiConsole.printHeader("WORKFLOW EXECUTION COMPLETED");
        boolean passed = resultCtx.getStatus() == TaskStatus.COMPLETED;
        String statusColor = passed ? AnsiConsole.GREEN : AnsiConsole.RED;

        System.out.printf("Status: %s | Total Duration: %d ms | Workflow ID: %s%n",
                AnsiConsole.color(resultCtx.getStatus().name(), statusColor),
                elapsed,
                resultCtx.getWorkflowId());

        if (resultCtx.getVerificationResult() != null) {
            VerificationResult vr = resultCtx.getVerificationResult();
            System.out.printf("Verifier Score: %s (%s)%n",
                    AnsiConsole.bold(vr.score() + "/100"),
                    vr.passed() ? AnsiConsole.green("PASSED") : AnsiConsole.red("FAILED"));
            System.out.printf("Verifier Summary: %s%n", vr.summary());

            if (!vr.checklist().isEmpty()) {
                System.out.println("\nVerification Checklist:");
                for (String item : vr.checklist()) {
                    System.out.printf("  %s %s%n", AnsiConsole.green("✓"), item);
                }
            }

            if (!vr.issues().isEmpty()) {
                System.out.println("\nDefects / Issues Detected:");
                for (String issue : vr.issues()) {
                    System.out.printf("  %s %s%n", AnsiConsole.red("✗"), issue);
                }
            }
        }

        System.out.printf("%nSaved JSON & Markdown logs in: %s%n",
                AnsiConsole.cyan(engine.getStorageService().getLogsDirectory().toString()));

        readInput("\nPress Enter to return to main menu...");
    }

    private void handleViewHistory() {
        AnsiConsole.printHeader("SAVED WORKFLOW RUNS & HISTORY");
        List<Path> runs = engine.getStorageService().listSavedRuns();

        if (runs.isEmpty()) {
            System.out.println(AnsiConsole.yellow("No saved workflow runs found yet. Execute a task first!"));
            readInput("\nPress Enter to continue...");
            return;
        }

        System.out.printf("Found %d saved execution run(s):%n%n", runs.size());
        for (int i = 0; i < Math.min(runs.size(), 10); i++) {
            System.out.printf("  [%d] %s%n", i + 1, runs.get(i).getFileName());
        }
        System.out.println("  [B] Back to Main Menu");

        String choice = readInput("\nSelect a run number to inspect (or B to return): ");
        if (choice.equalsIgnoreCase("b") || choice.isBlank()) {
            return;
        }

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < runs.size()) {
                Path selected = runs.get(index);
                String content = engine.getStorageService().readRun(selected);
                AnsiConsole.printHeader("FILE CONTENT: " + selected.getFileName());
                System.out.println(content);
            } else {
                System.out.println(AnsiConsole.red("Selection out of range."));
            }
        } catch (NumberFormatException e) {
            System.out.println(AnsiConsole.red("Invalid number input."));
        } catch (Exception e) {
            System.out.println(AnsiConsole.red("Failed to read run file: " + e.getMessage()));
        }

        readInput("\nPress Enter to continue...");
    }

    private void handleInspectComponents() {
        AnsiConsole.printHeader("INSPECT SYSTEM ARCHITECTURE & COMPONENTS");

        System.out.println(AnsiConsole.bold("1. SPECIALIZED AGENTS:"));
        System.out.printf("  • %s: %s%n", AnsiConsole.cyan("PlannerAgent"),
                "Deconstructs goals into dependency task graphs; generates remediation subtasks.");
        System.out.printf("  • %s: %s%n", AnsiConsole.yellow("ExecutorAgent"),
                "Instantiates TaskCommands and runs them against configured tool execution strategies.");
        System.out.printf("  • %s: %s%n", AnsiConsole.magenta("VerifierAgent"),
                "Audits deliverables, evaluates acceptance criteria, calculates quality score (0-100).");

        System.out.println(AnsiConsole.bold("\n2. REGISTERED TOOLS:"));
        for (Tool tool : engine.getToolRegistry().getAllTools()) {
            System.out.printf("  • %s : %s%n", AnsiConsole.green(tool.getName()), tool.getDescription());
            System.out.printf("    Parameters: %s | Supports Undo/Compensation: %s%n",
                    tool.getParameterSchema().keySet(), tool.canUndo());
        }

        System.out.println(AnsiConsole.bold("\n3. DYNAMIC EXECUTION STRATEGIES:"));
        Map<String, ExecutionStrategy> strategies = engine.getStrategyRegistry().getAllStrategies();
        for (Map.Entry<String, ExecutionStrategy> entry : strategies.entrySet()) {
            boolean isActive = entry.getValue() == engine.getStrategyRegistry().getActiveStrategy();
            System.out.printf("  • [%s] %s %s: %s%n",
                    entry.getKey(),
                    entry.getValue().getName(),
                    isActive ? AnsiConsole.green("(ACTIVE)") : "",
                    entry.getValue().getDescription());
        }

        readInput("\nPress Enter to continue...");
    }

    private void handleConfigureStrategy() {
        AnsiConsole.printHeader("CONFIGURE DYNAMIC EXECUTION STRATEGY");
        System.out.println("Select the strategy to use for dispatching tool commands:");
        System.out.println("  [1] Deterministic (Direct execution with calibrated local latency)");
        System.out.println("  [2] Simulated Cloud API (Simulates remote REST microservice latency & headers)");
        System.out.println("  [3] Fault-Tolerant (Automatic retry with backoff & graceful fallback)");
        System.out.println("  [B] Cancel / Keep Current");
        System.out.println();

        String choice = readInput("Select Strategy (1-3): ");
        switch (choice) {
            case "1" -> {
                engine.getStrategyRegistry().setActiveStrategy("deterministic");
                System.out.println(AnsiConsole.green("Strategy changed to: Deterministic"));
            }
            case "2" -> {
                engine.getStrategyRegistry().setActiveStrategy("api");
                System.out.println(AnsiConsole.green("Strategy changed to: Simulated Cloud API"));
            }
            case "3" -> {
                engine.getStrategyRegistry().setActiveStrategy("fallback");
                System.out.println(AnsiConsole.green("Strategy changed to: Fault-Tolerant (Retry & Fallback)"));
            }
            default -> System.out.println(AnsiConsole.yellow("No changes made."));
        }
    }

    private void handleExplainArchitecture() {
        AnsiConsole.printHeader("DESIGN PATTERNS & MODULAR ARCHITECTURE");
        String text = """
            1. STRATEGY PATTERN (agentflow.strategy):
               - ExecutionStrategy defines dynamic execution policies.
               - Implementations: DeterministicExecutionStrategy, SimulatedApiStrategy,
                 FaultTolerantFallbackStrategy.
               - Decouples task execution behavior from agent domain logic.

            2. COMMAND PATTERN (agentflow.command):
               - TaskCommand encapsulates an executable action (ToolCallCommand).
               - Provides execute() and undo() (compensation/rollback).
               - CompositeTaskCommand enables transactional rollback across pipelines.
               - CommandHistory tracks execution for auditing and replay.

            3. STATE MACHINE & DETERMINISTIC TRANSITIONS (agentflow.core):
               - Strict lifecycle states: PENDING -> RUNNING -> COMPLETED / FAILED / ROLLED_BACK.
               - Illegal transitions throw IllegalStateException with detailed diagnostics.
               - Real-time event broadcasting to decoupled listeners.

            4. SPECIALIZED AUTONOMOUS AGENTS (agentflow.agent):
               - PlannerAgent: Problem decomposition, dependency graph creation, adaptive replan.
               - ExecutorAgent: Command dispatching, dependency resolution, failure containment.
               - VerifierAgent: Quality assertion, acceptance scoring, defect identification.
            """;
        System.out.println(text);
        readInput("Press Enter to continue...");
    }

    private String readInput(String prompt) {
        System.out.print(prompt);
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            } else {
                running = false;
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private static String indentBlock(String block, String prefix) {
        if (block == null) return "";
        return block.replace("\n", "\n" + prefix);
    }
}
