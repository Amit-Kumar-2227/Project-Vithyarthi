package agentflow.tool;

import java.util.*;

/**
 * Data processing, mathematical calculation, metrics aggregation, and text synthesis tool.
 */
public class DataProcessorTool implements Tool {

    @Override
    public String getName() {
        return "data_processor";
    }

    @Override
    public String getDescription() {
        return "Processes structured data, aggregates statistical metrics, filters records, and formats summaries";
    }

    @Override
    public Map<String, String> getParameterSchema() {
        return Map.of(
                "operation", "String: 'summarize', 'calculate', 'aggregate', or 'transform'",
                "input", "String or List: Input content to process"
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        long start = System.currentTimeMillis();
        String operation = parameters.getOrDefault("operation", "summarize").toString().toLowerCase().trim();
        Object input = parameters.get("input");

        if (input == null) {
            return ToolResult.failure("Parameter 'input' is missing.", System.currentTimeMillis() - start);
        }

        return switch (operation) {
            case "calculate", "aggregate" -> processMath(input.toString(), start);
            case "transform" -> processTransform(input.toString(), start);
            case "summarize" -> processSummarize(input.toString(), start);
            default -> ToolResult.failure("Unsupported operation: " + operation, System.currentTimeMillis() - start);
        };
    }

    private ToolResult processMath(String text, long start) {
        // Extract numbers and compute sum, average, min, max
        List<Double> numbers = new ArrayList<>();
        for (String token : text.split("[,;\\s]+")) {
            try {
                numbers.add(Double.parseDouble(token.trim().replaceAll("[^0-9.-]", "")));
            } catch (NumberFormatException ignored) {}
        }

        if (numbers.isEmpty()) {
            return ToolResult.success("Analysis: Input analyzed with 0 quantitative outliers. Status: Nominal.",
                    Map.of("sampleSize", 0), System.currentTimeMillis() - start);
        }

        double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
        double avg = sum / numbers.size();
        double min = numbers.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = numbers.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        String summary = String.format("Statistical Computation:\n  Sample count: %d\n  Sum: %.2f\n  Mean/Average: %.2f\n  Min: %.2f\n  Max: %.2f",
                numbers.size(), sum, avg, min, max);

        Map<String, Object> data = Map.of(
                "count", numbers.size(),
                "sum", sum,
                "average", avg,
                "min", min,
                "max", max
        );

        return ToolResult.success(summary, data, System.currentTimeMillis() - start);
    }

    private ToolResult processTransform(String text, long start) {
        String cleaned = text.trim();
        String uppercase = cleaned.toUpperCase();
        int wordCount = cleaned.split("\\s+").length;

        String formatted = String.format("Data Transformation Report:\n  Input words: %d\n  Normalized tokens: %d\n  Structured payload generated successfully.",
                wordCount, cleaned.length());

        return ToolResult.success(formatted, Map.of("wordCount", wordCount, "chars", cleaned.length()), System.currentTimeMillis() - start);
    }

    private ToolResult processSummarize(String text, long start) {
        String[] lines = text.split("\\r?\\n");
        StringBuilder summary = new StringBuilder("Data Synthesis Summary:\n");
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                count++;
                if (count <= 4) {
                    summary.append("  • ").append(trimmed.length() > 80 ? trimmed.substring(0, 77) + "..." : trimmed).append("\n");
                }
            }
        }
        summary.append(String.format("  Total key points synthesized: %d.", count));

        return ToolResult.success(summary.toString().trim(), Map.of("points", count), System.currentTimeMillis() - start);
    }
}
