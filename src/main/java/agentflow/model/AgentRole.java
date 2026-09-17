package agentflow.model;

/**
 * Defines the specialized responsibilities of an autonomous agent in the workflow.
 */
public enum AgentRole {
    PLANNER("Planner Agent", "Deconstructs complex goals into structured sub-tasks"),
    EXECUTOR("Executor Agent", "Dispatches and executes task commands and tool actions"),
    VERIFIER("Verifier Agent", "Audits results, validates acceptance criteria, and scores quality"),
    COORDINATOR("Coordinator Agent", "Orchestrates multi-agent communication and lifecycle");

    private final String displayName;
    private final String description;

    AgentRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
