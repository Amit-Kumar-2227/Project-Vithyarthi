package agentflow.command;

import java.util.*;

/**
 * Tracks executed commands, maintaining execution history and supporting undo operations.
 */
public class CommandHistory {
    private final List<TaskCommand> history = new ArrayList<>();
    private final Stack<TaskCommand> executedStack = new Stack<>();

    public synchronized void record(TaskCommand command) {
        if (command != null) {
            history.add(command);
            executedStack.push(command);
        }
    }

    public synchronized Optional<TaskCommand> undoLast() {
        if (!executedStack.isEmpty()) {
            TaskCommand cmd = executedStack.pop();
            cmd.undo();
            return Optional.of(cmd);
        }
        return Optional.empty();
    }

    public synchronized void undoAll() {
        while (!executedStack.isEmpty()) {
            TaskCommand cmd = executedStack.pop();
            try {
                cmd.undo();
            } catch (Exception e) {
                System.err.println("Undo failure: " + e.getMessage());
            }
        }
    }

    public synchronized List<TaskCommand> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public synchronized void clear() {
        history.clear();
        executedStack.clear();
    }
}
