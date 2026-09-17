package agentflow.ui;

import agentflow.model.AgentRole;
import agentflow.model.TaskStatus;

/**
 * Terminal UI helper providing ANSI colors, styled boxes, and formatted status badges.
 */
public class AnsiConsole {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_RED = "\u001B[41m";

    private static boolean ansiSupported = true;

    static {
        // Check if color is explicitly disabled
        String noColor = System.getenv("NO_COLOR");
        if (noColor != null && !noColor.isBlank()) {
            ansiSupported = false;
        }
    }

    public static void setAnsiSupported(boolean supported) {
        ansiSupported = supported;
    }

    public static boolean isAnsiSupported() {
        return ansiSupported;
    }

    public static String color(String text, String colorCode) {
        if (!ansiSupported) return text;
        return colorCode + text + RESET;
    }

    public static String bold(String text) {
        if (!ansiSupported) return text;
        return BOLD + text + RESET;
    }

    public static String dim(String text) {
        if (!ansiSupported) return text;
        return DIM + text + RESET;
    }

    public static String cyan(String text) {
        return color(text, CYAN);
    }

    public static String green(String text) {
        return color(text, GREEN);
    }

    public static String yellow(String text) {
        return color(text, YELLOW);
    }

    public static String red(String text) {
        return color(text, RED);
    }

    public static String magenta(String text) {
        return color(text, MAGENTA);
    }

    public static String blue(String text) {
        return color(text, BLUE);
    }

    public static String statusBadge(TaskStatus status) {
        if (status == null) return "[UNKNOWN]";
        return switch (status) {
            case PENDING -> color("[ PENDING ]", YELLOW);
            case RUNNING -> color("[ RUNNING ]", CYAN);
            case COMPLETED -> color("[COMPLETED]", GREEN);
            case FAILED -> color("[ FAILED  ]", RED);
            case SKIPPED -> color("[ SKIPPED ]", WHITE);
            case ROLLED_BACK -> color("[ROLLBACK ]", MAGENTA);
        };
    }

    public static String agentBadge(AgentRole role) {
        if (role == null) return "[AGENT]";
        return switch (role) {
            case PLANNER -> color(BOLD + "[PLANNER]  ", BLUE);
            case EXECUTOR -> color(BOLD + "[EXECUTOR] ", YELLOW);
            case VERIFIER -> color(BOLD + "[VERIFIER] ", MAGENTA);
            case COORDINATOR -> color(BOLD + "[ORCHESTR]", CYAN);
        };
    }

    public static void printBanner() {
        String art = """
            ======================================================================
              ___   ____ _____ _   _ _____ _____ _     _____ _     _   _ _____ 
             / _ \\ / ___| ____| \\ | |_   _|  ___| |   / _ \\ \\ \\   / / | | ____|
            / /_\\ \\ |  _|  _| |  \\| | | | | |_  | |  | | | | \\ \\ / /  | |  _|  
            |  _  | |_| | |___| |\\  | | | |  _| | |__| |_| |  \\ V /   | | |___ 
            |_| |_|\\____|_____|_| \\_| |_| |_|   |_____\\___/    \\_/    |_|_____|
             >> Modular Autonomous Multi-Agent Orchestration Framework (Java 17) <<
            ======================================================================
            """;
        System.out.println(cyan(art));
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(cyan("----------------------------------------------------------------------"));
        System.out.println(bold(" " + title));
        System.out.println(cyan("----------------------------------------------------------------------"));
    }

    public static void printBox(String title, String content) {
        int width = 70;
        String line = "=".repeat(width);
        System.out.println(cyan(line));
        System.out.println(bold("  " + title));
        System.out.println(cyan("-".repeat(width)));
        for (String l : content.split("\\r?\\n")) {
            System.out.println("  " + l);
        }
        System.out.println(cyan(line));
    }
}
