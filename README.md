# Modular Agentic Workflow System (Java 17+)

A production-ready, zero-dependency Command-Line Java framework implementing an autonomous, modular **Multi-Agent Workflow & Task Automation System**.

Built from the ground up to demonstrate enterprise agentic design principles, this project leverages the **Strategy** and **Command** design patterns, strict **State Machine** transitions (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `ROLLED_BACK`, `SKIPPED`), specialized agent roles (**Planner**, **Executor**, **Verifier**), real-time ANSI terminal telemetry, and persistent execution auditing in both **JSON** and **Markdown** formats.

---

## Architecture Overview

```
                                 +-------------------------+
                                 |  Terminal CLI Interface |
                                 |   (Interactive Menu &   |
                                 |    Real-time ANSI UI)   |
                                 +------------+------------+
                                              |
                                              v
+------------------------------------------------------------------------------------------+
|                                     WorkflowEngine                                       |
|                  (Lifecycle Orchestration, Replan Loop, Event Bus)                       |
+---------------------------------------------+--------------------------------------------+
                                              |
     +----------------------------------------+----------------------------------------+
     |                                        |                                        |
     v                                        v                                        v
+-------------------------+      +-------------------------+      +-------------------------+
|      PlannerAgent       |      |      ExecutorAgent      |      |      VerifierAgent      |
|  • Task Decomposition   |      |  • Dependency Sorting   |      |  • Acceptance Checklist |
|  • Directed Dependency  |      |  • Command Dispatching  |      |  • Confidence Scoring   |
|    Graph Generation     |      |  • Execution Recovery   |      |  • Defect Detection     |
|  • Adaptive Replanning  |      |                         |      |  • Synthesis Report     |
+-------------------------+      +------------+------------+      +-------------------------+
                                              |
                                              v
                          +---------------------------------------+
                          |            Command Pattern            |
                          |  • TaskCommand (Interface)            |
                          |  • ToolCallCommand (Tool Execution)   |
                          |  • CompositeTaskCommand (Transaction) |
                          |  • CommandHistory (Undo / Rollback)   |
                          +-------------------+-------------------+
                                              |
                                              v
                          +---------------------------------------+
                          |           Strategy Pattern            |
                          |  • ExecutionStrategy (Interface)      |
                          |  • DeterministicExecutionStrategy     |
                          |  • SimulatedApiStrategy (REST Cloud)  |
                          |  • FaultTolerantFallbackStrategy      |
                          +-------------------+-------------------+
                                              |
                                              v
                          +---------------------------------------+
                          |             Tool Registry             |
                          |  • WebSearchTool                      |
                          |  • FileSystemTool (Sandboxed & Undo)  |
                          |  • DataProcessorTool                  |
                          |  • CodeAnalysisTool                   |
                          |  • SystemNotificationTool             |
                          +-------------------+-------------------+
                                              |
                                              v
                          +---------------------------------------+
                          |           Storage & Auditing          |
                          |  • JsonSerializer (Zero-Dep Indented) |
                          |  • Markdown Timeline Generator        |
                          |  • ./workflow_logs/ (Persisted Runs)  |
                          +---------------------------------------+
```

---

## Key Design Patterns & Engineering Highlights

### 1. Strategy Pattern (`agentflow.strategy`)
Decouples how tool calls are executed from the agent domain logic:
* **`DeterministicExecutionStrategy`**: Standard direct execution with calibrated local latency, providing predictable, reproducible agent behavior.
* **`SimulatedApiStrategy`**: Injects simulated cloud REST network latency, simulated HTTP status codes, and request tracking IDs (`req-*`).
* **`FaultTolerantFallbackStrategy`**: Provides automatic retries with exponential backoff and safe degraded fallback mitigation when exceptions occur.
* **`StrategyRegistry`**: Enables dynamic runtime hot-swapping of active strategies via the CLI menu.

### 2. Command Pattern (`agentflow.command`)
Encapsulates all workflow operations as standalone, executable command objects:
* **`TaskCommand`**: Common interface exposing `execute()`, `undo()`, `getTask()`, and `getStatus()`.
* **`ToolCallCommand`**: Dispatches specific tool calls, updates task status through the state machine, records execution duration, and supports compensation (`undo()`).
* **`CompositeTaskCommand`**: Chains multi-step sub-tasks into a transactional unit. If any step fails, all previously completed steps are automatically rolled back in reverse order (Saga pattern).
* **`CommandHistory`**: Audits all executed commands and provides single-step or total pipeline rollback.

### 3. State Management & State Machine (`agentflow.core.StateMachine`)
Enforces strict deterministic state transitions:
* **Lifecycle States**:
  * `PENDING` $\to$ `RUNNING`, `SKIPPED`, `FAILED`
  * `RUNNING` $\to$ `COMPLETED`, `FAILED`, `ROLLED_BACK`
  * `FAILED` $\to$ `PENDING` (re-try), `RUNNING`, `ROLLED_BACK`
  * `COMPLETED` $\to$ `ROLLED_BACK` (compensation)
* Attempting an illegal transition (e.g., `PENDING` $\to$ `COMPLETED`) instantly throws an `IllegalStateException`.
* Real-time state transition events are broadcasted to decoupled listeners (`WorkflowEventListener`).

### 4. Specialized Autonomous Multi-Agent Hierarchy (`agentflow.agent`)
* **`PlannerAgent`**: Examines the user's objective, infers the problem domain (code refactor, security audit, market research, SRE triage, finance), and builds an acyclic task graph with tool bindings and dependency edges. Also handles **adaptive replanning** if the Verifier flags defects.
* **`ExecutorAgent`**: Validates dependency completion, resolves tools from the registry, dispatches `TaskCommand`s using the active `ExecutionStrategy`, and captures structured outputs.
* **`VerifierAgent`**: Performs post-execution audits: verifies 100% task completion, verifies deliverable integrity, calculates a composite quality score (0–100%), produces an assertion checklist, and triggers replan loops if criteria are not satisfied.

---

## Directory & Package Structure

```
modular-agentic-workflow/
├── pom.xml                               # Standard Maven 3+ build descriptor
├── build.bat                             # Windows single-command build script
├── run.bat                               # Windows single-command execution script
├── test.bat                              # Windows automated test suite runner
├── build.sh                              # Unix/macOS build script
├── run.sh                                # Unix/macOS run script
├── test.sh                               # Unix/macOS test script
├── README.md                             # Comprehensive technical documentation
├── src/
│   ├── main/java/agentflow/
│   │   ├── Main.java                     # Application entry point (CLI & direct execution)
│   │   ├── model/                        # Domain Models & Context
│   │   │   ├── TaskStatus.java           # State enum with legality validator
│   │   │   ├── AgentRole.java            # Specialized agent role definitions
│   │   │   ├── Task.java                 # Granular task unit with dependencies
│   │   │   ├── ExecutionLogEntry.java    # Timestamped telemetry log record
│   │   │   ├── VerificationResult.java   # Scoring, checklist, and issue report
│   │   │   ├── AgentResult.java          # Agent lifecycle return payload
│   │   │   └── WorkflowContext.java      # Shared blackboard / state bus
│   │   ├── core/                         # Orchestration & State Engine
│   │   │   ├── StateMachine.java         # Transition enforcement & event emitter
│   │   │   ├── WorkflowEventListener.java# Real-time event listener contract
│   │   │   └── WorkflowEngine.java       # Master coordinator & replanning loop
│   │   ├── strategy/                     # Strategy Pattern Implementation
│   │   │   ├── ExecutionStrategy.java    # Strategy contract
│   │   │   ├── DeterministicExecutionStrategy.java
│   │   │   ├── SimulatedApiStrategy.java
│   │   │   ├── FaultTolerantFallbackStrategy.java
│   │   │   └── StrategyRegistry.java     # Dynamic strategy discovery & switching
│   │   ├── command/                      # Command Pattern Implementation
│   │   │   ├── TaskCommand.java          # Command contract
│   │   │   ├── ToolCallCommand.java      # Concrete tool execution command
│   │   │   ├── CompositeTaskCommand.java # Transactional composite with rollback
│   │   │   └── CommandHistory.java       # Execution audit stack & undo history
│   │   ├── tool/                         # Tool Registry & Concrete Tools
│   │   │   ├── Tool.java                 # Tool contract with compensation hooks
│   │   │   ├── ToolResult.java           # Standardized output payload
│   │   │   ├── ToolRegistry.java         # Tool discovery registry
│   │   │   ├── WebSearchTool.java        # Simulated search engine & citations
│   │   │   ├── FileSystemTool.java       # Sandboxed file I/O with undo
│   │   │   ├── DataProcessorTool.java    # Calculation, synthesis & statistics
│   │   │   ├── CodeAnalysisTool.java     # AST/linting, security, and quality audit
│   │   │   └── SystemNotificationTool.java # Alerts and stakeholder digests
│   │   ├── storage/                      # Persistence & Serialization
│   │   │   ├── JsonSerializer.java       # Pure Java zero-dependency JSON builder
│   │   │   └── WorkflowStorageService.java# JSON and Markdown run persistence
│   │   └── ui/                           # Terminal User Interface
│   │       ├── AnsiConsole.java          # ANSI color palette, badges & boxes
│   │       └── CliMenu.java              # Interactive menu & real-time monitor
│   └── test/java/agentflow/
│       └── WorkflowVerificationTest.java # 27-point comprehensive automated test suite
└── workflow_logs/                        # Auto-generated JSON and Markdown audit runs
```

---

## Environment & Prerequisites

* **Java Development Kit**: JDK 17 or higher (tested with Microsoft OpenJDK 17 / Eclipse Adoptium).
* **Dependencies**: **Zero third-party libraries**. Uses exclusively standard Java standard library packages (`java.time`, `java.nio`, `java.util.concurrent`, etc.).
* **Operating Systems**: Windows, Linux, or macOS.

---

## Step-by-Step Build & Run Instructions

### Option 1: Using Included Scripts (Recommended)

#### On Windows:
```cmd
# 1. Compile the project
build.bat

# 2. Run the interactive CLI menu
run.bat

# 3. Run non-interactive direct goal execution
run.bat --run "Audit repository for security vulnerabilities and refactor concurrency handlers"

# 4. Run automated test suite
test.bat
```

#### On Linux / macOS:
```bash
chmod +x *.sh

# 1. Compile the project
./build.sh

# 2. Run the interactive CLI menu
./run.sh

# 3. Run non-interactive direct goal execution
./run.sh --run "Conduct enterprise market research on AI agent benchmarks"

# 4. Run automated test suite
./test.sh
```

---

### Option 2: Using Pure `javac` & `java` (No Build Tool Required)

```bash
# Compile source classes
javac -encoding UTF-8 -d bin $(find src/main/java -name "*.java")

# Run Interactive CLI
java -Dfile.encoding=UTF-8 -cp bin agentflow.Main

# Run Direct Workflow Execution
java -Dfile.encoding=UTF-8 -cp bin agentflow.Main --run "Perform cloud incident triage for latency alert"

# Compile and run automated test suite
javac -encoding UTF-8 -cp bin -d bin $(find src/test/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp bin agentflow.WorkflowVerificationTest
```

---

### Option 3: Using Apache Maven

```bash
# Compile and package executable JAR
mvn clean package

# Run with Maven Exec Plugin
mvn exec:java

# Run packaged standalone JAR
java -jar target/modular-agentic-workflow-1.0.0.jar
```

---

## Interactive CLI Walkthrough

When launched, the system displays the interactive main control menu:

```
======================================================================
  ___   ____ _____ _   _ _____ _____ _     _____ _     _   _ _____ 
 / _ \ / ___| ____| \ | |_   _|  ___| |   / _ \ \ \   / / | | ____|
/ /_\ \ |  _|  _| |  \| | | | | |_  | |  | | | | \ \ / /  | |  _|  
|  _  | |_| | |___| |\  | | | |  _| | |__| |_| |  \ V /   | | |___ 
|_| |_|\____|_____|_| \_| |_| |_|   |_____\___/    \_/    |_|_____|
 >> Modular Autonomous Multi-Agent Orchestration Framework (Java 17) <<
======================================================================

========================= [ MAIN CONTROL MENU ] =========================
  1.  Enter Custom Task / Query
  2.  Run Pre-configured Demo Scenarios
  3.  View Workflow Execution History & Logs (JSON/Markdown)
  4.  Inspect Agents, Registered Tools & Dynamic Strategies
  5.  Configure Dynamic Execution Strategy
  6.  System Architecture & Design Patterns Overview
  7.  Exit
=========================================================================
  Active Execution Strategy: Deterministic (Direct)
```

### Pre-Configured Demo Scenarios

Selecting **Option 2** displays pre-built scenarios demonstrating varied agent capabilities:
1. **Automated Code Audit & Security Refactoring**: Static analysis, CVE detection, concurrency best practices, sandboxed refactored source generation, and automated team notifications.
2. **Enterprise Market Intelligence & Competitor Report**: Web research synthesis, quantitative adoption aggregation, dossier compilation, and executive briefings.
3. **Cloud SRE Incident Triage & Auto-Remediation**: Incident signature lookup, latency distribution analysis, postmortem generation, and on-call alerting.
4. **Quarterly Financial Earnings Aggregation & Ratio Modeling**: SEC filing search, quarterly margin aggregation, CSV generation, and financial alerts.

### Real-Time Pipeline Telemetry

During workflow execution, state transitions and agent decisions are streamed in real time:

```
 [ORCHESTR] INIT -> [ RUNNING ] (Reason: Workflow initialized for goal: ...)
 [PLANNER]   Generated sub-task [task-1]: Static Code & Security Inspection (Tool: code_analysis, Deps: [])
 [PLANNER]   Generated sub-task [task-2]: Research Refactoring Patterns (Tool: web_search, Deps: [task-1])
 [PLANNER]   Generated sub-task [task-3]: Generate Refactored Source (Tool: file_system, Deps: [task-2])
 [PLANNER]   Plan generated successfully with 3 ordered sub-tasks.
 [EXECUTOR]  Starting task execution pipeline using Strategy: [Deterministic (Direct)]
 [EXECUTOR]  [task-1] Node [task-1] (code_analysis): [ PENDING ] -> [ RUNNING ]
 [EXECUTOR]  [task-1] Node [task-1] (code_analysis): [ RUNNING ] -> [COMPLETED] [Tool completed in 134 ms]
 [VERIFIER]  Initiating verification audit against goal...
 [VERIFIER]  Verification finished with Score 100/100 -> PASSED
 [ORCHESTR]  Workflow State: RUNNING -> [COMPLETED] (Reason: All pipeline stages completed and verified successfully.)
```

---

## Persistence & Audit Logs

Every workflow execution automatically produces two audit records in `./workflow_logs/`:

1. **Structured JSON (`run_<timestamp>_<workflowId>.json`)**:
   Full machine-readable snapshot containing workflow metadata, duration, all sub-tasks with parameters, results, errors, dependency graphs, full execution logs, and verification assertions.
2. **Markdown Timeline (`run_<timestamp>_<workflowId>.md`)**:
   Human-readable executive audit report featuring pipeline summary tables, chronological agent execution steps, and verification checklists.

---

## Verification & Automated Testing

The project includes an automated test harness (`WorkflowVerificationTest.java`) containing 27 unit and integration test assertions:
- **State Machine Rules**: Enforces valid transitions and asserts that illegal jumps throw `IllegalStateException`.
- **Planner Decomposition**: Validates subtask count, acyclic dependencies, and domain-specific plans.
- **Command & Compensation**: Asserts `ToolCallCommand` execution, disk file creation, and transactional rollback (`undo()`).
- **Composite Rollback**: Verifies multi-step composite rollback cleans up earlier artifacts on subsequent failure.
- **Dynamic Strategies**: Tests `Deterministic`, `SimulatedApi`, and `FaultTolerantFallback` strategies.
- **Verifier Scoring**: Verifies confidence score calculation, acceptance checklists, and defect detection.
- **Persistence Integrity**: Verifies zero-dependency JSON serialization and file existence.

Run the test suite at any time via:
```cmd
test.bat
```
Output:
```
=================================================
  TEST RESULTS: 27/27 PASSED
=================================================
All verification tests passed successfully!
```
