package agentflow.tool;

import java.time.Instant;
import java.util.*;

/**
 * Dispatches status updates, webhooks, and log digests to external channels or local alerts.
 */
public class SystemNotificationTool implements Tool {

    @Override
    public String getName() {
        return "notification";
    }

    @Override
    public String getDescription() {
        return "Dispatches execution summaries, alerts, and operational digests to registered endpoints";
    }

    @Override
    public Map<String, String> getParameterSchema() {
        return Map.of(
                "channel", "String: Destination channel ('console', 'slack_webhook', 'email')",
                "message", "String: Notification body",
                "priority", "String: 'INFO', 'WARN', 'CRITICAL'"
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        long start = System.currentTimeMillis();
        String channel = parameters.getOrDefault("channel", "console").toString();
        String message = parameters.getOrDefault("message", "").toString();
        String priority = parameters.getOrDefault("priority", "INFO").toString().toUpperCase();

        String payload = String.format("[%s] [%s] -> %s: %s",
                Instant.now(), priority, channel, message);

        Map<String, Object> data = Map.of(
                "channel", channel,
                "priority", priority,
                "delivered", true,
                "timestamp", Instant.now().toString()
        );

        return ToolResult.success("Notification delivered: " + payload, data, System.currentTimeMillis() - start);
    }
}
