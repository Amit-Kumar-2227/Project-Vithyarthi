package agentflow.tool;

import java.util.*;

/**
 * Pseudo-tool simulating web search with query matching, snippet generation, and source indexing.
 */
public class WebSearchTool implements Tool {

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "Searches online indexed resources, documentation, and industry benchmarks";
    }

    @Override
    public Map<String, String> getParameterSchema() {
        return Map.of(
                "query", "String: Search keywords or phrases",
                "maxResults", "Integer: Maximum results to return (default 3)"
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        long start = System.currentTimeMillis();
        String query = parameters.getOrDefault("query", "").toString().trim();
        if (query.isEmpty()) {
            return ToolResult.failure("Parameter 'query' cannot be blank.", System.currentTimeMillis() - start);
        }

        int max = 3;
        if (parameters.containsKey("maxResults")) {
            try {
                max = Integer.parseInt(parameters.get("maxResults").toString());
            } catch (NumberFormatException ignored) {}
        }

        List<Map<String, String>> mockResults = generateResultsForQuery(query, max);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Web search executed for query: \"%s\" (Found %d citations):\n", query, mockResults.size()));

        for (int i = 0; i < mockResults.size(); i++) {
            Map<String, String> r = mockResults.get(i);
            sb.append(String.format("  [%d] %s\n      Source: %s\n      Snippet: %s\n",
                    i + 1, r.get("title"), r.get("url"), r.get("snippet")));
        }

        Map<String, Object> data = Map.of(
                "query", query,
                "count", mockResults.size(),
                "citations", mockResults
        );

        return ToolResult.success(sb.toString().trim(), data, System.currentTimeMillis() - start);
    }

    private List<Map<String, String>> generateResultsForQuery(String query, int max) {
        List<Map<String, String>> results = new ArrayList<>();
        String lower = query.toLowerCase();

        if (lower.contains("java") || lower.contains("refactor") || lower.contains("code")) {
            results.add(Map.of(
                    "title", "Modern Java Best Practices: Records, Sealed Classes & Pattern Matching",
                    "url", "https://openjdk.org/jeps/patterns-records-2026",
                    "snippet", "Leveraging immutable domain models with records reduces boilerplate and ensures thread safety in concurrent pipelines."
            ));
            results.add(Map.of(
                    "title", "Effective Java Concurrency: Virtual Threads and State Management",
                    "url", "https://docs.oracle.com/en/java/concurrency-guidelines.html",
                    "snippet", "Deterministic state machines with explicit transition guards prevent race conditions in autonomous agent workflows."
            ));
        } else if (lower.contains("market") || lower.contains("competitor") || lower.contains("trend")) {
            results.add(Map.of(
                    "title", "Global Enterprise AI Agent Market Analysis (2025-2030)",
                    "url", "https://gartner-simulated.com/reports/ai-agents-market",
                    "snippet", "Modular agentic architectures saw a 145% year-over-year adoption in multi-step task automation across enterprise devops."
            ));
            results.add(Map.of(
                    "title", "Benchmark Analysis: Autonomous Workflow Orchestration Engines",
                    "url", "https://tech-benchmarks.org/orchestration-2026",
                    "snippet", "Decoupling Planner, Executor, and Verifier roles decreases hallucination and task failure rates from 28% to under 3.2%."
            ));
        } else if (lower.contains("incident") || lower.contains("triage") || lower.contains("diagnostic")) {
            results.add(Map.of(
                    "title", "SRE Playbook: Automated Incident Remediation with Verifier Loops",
                    "url", "https://sre-handbook.org/incident-triage-patterns",
                    "snippet", "Automated root-cause analysis requires read-only system inspection followed by verification assertions before rollouts."
            ));
            results.add(Map.of(
                    "title", "Distributed Tracing and Memory Leak Profiling in Cloud Services",
                    "url", "https://observability-standards.org/cloud-leaks-tracing",
                    "snippet", "High thread contention and unclosed connection pools are the leading indicators in 84% of high-latency alerts."
            ));
        } else {
            results.add(Map.of(
                    "title", "Domain Knowledge & Best Practices: " + query,
                    "url", "https://knowledge-base.internal/wiki/" + query.replaceAll("\\s+", "-"),
                    "snippet", "Synthesized domain insights, structural heuristics, and actionable execution patterns for " + query + "."
            ));
            results.add(Map.of(
                    "title", "Standards & Specification Framework for " + query,
                    "url", "https://standards.org/spec/" + query.replaceAll("\\s+", "_"),
                    "snippet", "Verified reference architecture and operational guidelines addressing primary objectives."
            ));
        }

        while (results.size() > max && !results.isEmpty()) {
            results.remove(results.size() - 1);
        }
        return results;
    }
}
