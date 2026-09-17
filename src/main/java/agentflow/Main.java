package agentflow;

import agentflow.core.WorkflowEngine;
import agentflow.model.WorkflowContext;
import agentflow.ui.AnsiConsole;
import agentflow.ui.CliMenu;

/**
 * Main application entry point for Modular Agentic Workflow System.
 */
public class Main {

    public static void main(String[] args) {
        WorkflowEngine engine = new WorkflowEngine();

        // Support non-interactive CLI direct query execution:
        // java -jar agentflow.jar --run "Goal statement"
        if (args.length >= 2 && "--run".equalsIgnoreCase(args[0])) {
            String goal = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            AnsiConsole.printBanner();
            System.out.println("Executing non-interactive workflow for goal: " + goal);
            WorkflowContext ctx = engine.executeWorkflow(goal);
            System.out.println(ctx.getFinalSummary());
            System.exit(ctx.getStatus() == agentflow.model.TaskStatus.COMPLETED ? 0 : 1);
            return;
        }

        if (args.length >= 1 && ("--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0]))) {
            System.out.println("Modular Agentic Workflow System (Java 17+)");
            System.out.println("Usage:");
            System.out.println("  java -cp bin agentflow.Main               (Interactive CLI Menu)");
            System.out.println("  java -cp bin agentflow.Main --run <goal>  (Non-interactive execution)");
            return;
        }

        // Default: Interactive Terminal CLI Menu
        CliMenu cli = new CliMenu(engine);
        cli.start();
    }
}
