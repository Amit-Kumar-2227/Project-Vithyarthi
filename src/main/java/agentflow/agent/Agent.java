package agentflow.agent;

import agentflow.core.StateMachine;
import agentflow.model.AgentResult;
import agentflow.model.AgentRole;
import agentflow.model.WorkflowContext;

/**
 * Fundamental interface for autonomous workflow agents.
 */
public interface Agent {

    /**
     * Display name of the agent.
     */
    String getName();

    /**
     * Role and specialization of the agent.
     */
    AgentRole getRole();

    /**
     * Executes the agent's responsibilities over the shared workflow context.
     *
     * @param context      the shared workflow blackboard
     * @param stateMachine the state machine for transitions and logging
     * @return AgentResult indicating outcome and status
     */
    AgentResult process(WorkflowContext context, StateMachine stateMachine);
}
