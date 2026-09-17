package agentflow.tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Sandboxed tool for reading, writing, and listing files in a local workspace directory.
 * Supports compensation / undo for rollback when writing files.
 */
public class FileSystemTool implements Tool {
    private final Path sandboxRoot;
    private final Map<String, String> backupHistory = new HashMap<>();

    public FileSystemTool(Path sandboxRoot) {
        this.sandboxRoot = sandboxRoot.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.sandboxRoot);
        } catch (IOException e) {
            System.err.println("Warning: Could not create sandbox root: " + e.getMessage());
        }
    }

    public FileSystemTool() {
        this(Path.of("./workspace_sandbox"));
    }

    @Override
    public String getName() {
        return "file_system";
    }

    @Override
    public String getDescription() {
        return "Performs sandboxed file operations: read, write, append, and list within the safe workspace";
    }

    @Override
    public Map<String, String> getParameterSchema() {
        return Map.of(
                "action", "String: 'read', 'write', 'append', or 'list'",
                "path", "String: Relative file or directory path",
                "content", "String: Content to write (for write/append)"
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        long start = System.currentTimeMillis();
        String action = parameters.getOrDefault("action", "list").toString().toLowerCase().trim();
        String relPath = parameters.getOrDefault("path", ".").toString().trim();

        try {
            Path target = sandboxRoot.resolve(relPath).normalize();
            if (!target.startsWith(sandboxRoot)) {
                return ToolResult.failure("Security violation: path traversal outside sandbox is forbidden.", System.currentTimeMillis() - start);
            }

            return switch (action) {
                case "read" -> handleRead(target, start);
                case "write" -> handleWrite(target, parameters.getOrDefault("content", "").toString(), false, start);
                case "append" -> handleWrite(target, parameters.getOrDefault("content", "").toString(), true, start);
                case "list" -> handleList(target, start);
                default -> ToolResult.failure("Unknown action: " + action, System.currentTimeMillis() - start);
            };
        } catch (Exception e) {
            return ToolResult.failure("File system error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    private ToolResult handleRead(Path target, long start) throws IOException {
        if (!Files.exists(target)) {
            return ToolResult.failure("File does not exist: " + sandboxRoot.relativize(target), System.currentTimeMillis() - start);
        }
        String content = Files.readString(target);
        return ToolResult.success("File Content (" + sandboxRoot.relativize(target) + "):\n" + content,
                Map.of("path", target.toString(), "bytes", content.length()), System.currentTimeMillis() - start);
    }

    private ToolResult handleWrite(Path target, String content, boolean append, long start) throws IOException {
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        String pathKey = target.toString();
        if (Files.exists(target) && !backupHistory.containsKey(pathKey)) {
            backupHistory.put(pathKey, Files.readString(target));
        }

        if (append && Files.exists(target)) {
            Files.writeString(target, content + System.lineSeparator(), java.nio.file.StandardOpenOption.APPEND);
        } else {
            Files.writeString(target, content);
        }

        return ToolResult.success(
                String.format("Successfully wrote %d chars to %s", content.length(), sandboxRoot.relativize(target)),
                Map.of("path", target.toString(), "writtenChars", content.length()),
                System.currentTimeMillis() - start
        );
    }

    private ToolResult handleList(Path target, long start) throws IOException {
        if (!Files.exists(target)) {
            return ToolResult.failure("Directory does not exist: " + sandboxRoot.relativize(target), System.currentTimeMillis() - start);
        }

        List<String> entries = new ArrayList<>();
        try (var stream = Files.list(target)) {
            stream.forEach(p -> entries.add((Files.isDirectory(p) ? "[DIR] " : "[FILE] ") + p.getFileName().toString()));
        }

        String output = entries.isEmpty() ? "Directory is empty." : String.join("\n", entries);
        return ToolResult.success("Listing for " + sandboxRoot.relativize(target) + ":\n" + output,
                Map.of("count", entries.size()), System.currentTimeMillis() - start);
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public void undo(Map<String, Object> parameters) {
        String relPath = parameters.getOrDefault("path", "").toString();
        Path target = sandboxRoot.resolve(relPath).normalize();
        String pathKey = target.toString();

        try {
            if (backupHistory.containsKey(pathKey)) {
                String previousContent = backupHistory.remove(pathKey);
                Files.writeString(target, previousContent);
            } else if (Files.exists(target)) {
                Files.delete(target);
            }
        } catch (IOException e) {
            System.err.println("Failed to undo file operation: " + e.getMessage());
        }
    }
}
