package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JSONUtil {
    private static final String LOGS_FILE = "src/main/resources/logs.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log a user login event
     * @param username The username of the user logging in
     */
    public static void logLogin(String username) {
        logEvent(username, "LOGIN");
    }

    /**
     * Log a user logout event
     * @param username The username of the user logging out
     */
    public static void logLogout(String username) {
        logEvent(username, "LOGOUT");
    }

    /**
     * Log an event to the logs.json file
     * @param username The username associated with the event
     * @param eventType The type of event (LOGIN, LOGOUT, etc.)
     */
    private static void logEvent(String username, String eventType) {
        try {
            List<String> logs = readLogs();
            
            String logEntry = String.format(
                "{\"username\":\"%s\",\"event\":\"%s\",\"timestamp\":\"%s\"}",
                escapeJson(username),
                eventType,
                LocalDateTime.now().format(DATE_FORMATTER)
            );
            
            logs.add(logEntry);
            writeLogs(logs);
        } catch (IOException e) {
            System.err.println("Error logging event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Read the logs from logs.json file
     * @return List of JSON strings containing all logs
     */
    private static List<String> readLogs() throws IOException {
        File logsFile = new File(LOGS_FILE);
        List<String> logs = new ArrayList<>();
        
        if (!logsFile.exists()) {
            return logs;
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(logsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        
        String fileContent = content.toString().trim();
        if (fileContent.isEmpty() || fileContent.equals("[]")) {
            return logs;
        }
        
        // Parse JSON array manually
        fileContent = fileContent.substring(1, fileContent.length() - 1); // Remove [ and ]
        if (!fileContent.isEmpty()) {
            String[] entries = fileContent.split("\\},\\{");
            for (String entry : entries) {
                entry = entry.replace("}", "").replace("{", "").trim();
                if (!entry.isEmpty()) {
                    logs.add("{" + entry + "}");
                }
            }
        }
        
        return logs;
    }

    /**
     * Write logs to logs.json file
     * @param logs List of JSON strings containing all logs
     */
    private static void writeLogs(List<String> logs) throws IOException {
        File logsFile = new File(LOGS_FILE);
        
        // Create parent directories if they don't exist
        logsFile.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(logsFile)) {
            writer.write("[\n");
            for (int i = 0; i < logs.size(); i++) {
                writer.write("  " + logs.get(i));
                if (i < logs.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("]\n");
            writer.flush();
        }
    }

    /**
     * Get all logs
     * @return List of JSON strings containing all logged events
     */
    public static List<String> getAllLogs() {
        try {
            return readLogs();
        } catch (IOException e) {
            System.err.println("Error reading logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get logs for a specific user
     * @param username The username to filter logs by
     * @return List of JSON strings containing logs for the user
     */
    public static List<String> getLogsForUser(String username) {
        List<String> allLogs = getAllLogs();
        List<String> userLogs = new ArrayList<>();
        
        for (String log : allLogs) {
            if (log.contains("\"username\":\"" + escapeJson(username) + "\"")) {
                userLogs.add(log);
            }
        }
        
        return userLogs;
    }

    /**
     * Escape special characters in JSON strings
     * @param str The string to escape
     * @return The escaped string
     */
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
