package org.example.ai.chatbot.domain.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class Utils {
    /**
     * Format response as Server-Sent Event with proper structure
     * @param event The event type (message, error, etc.)
     * @param code Status code
     * @param content Message content
     * @return Formatted SSE message
     */
    public static String formatSseMessage(String event, String code, String content) {
        String id = System.currentTimeMillis() + "-" + RandomStringUtils.randomAlphanumeric(8);
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(id).append("\n");
        sb.append("event: ").append(event).append("\n");
        sb.append("data: {");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"code\":\"").append(code).append("\",");
        sb.append("\"content\":").append(jsonEscape(content));
        sb.append("}\n\n");
        return sb.toString();
    }

    /**
     * Format response as Server-Sent Event with proper structure
     * @param event The event type (message, error, etc.)
     * @param content Message content
     * @return Formatted SSE message
     */
    public static String formatSseMessage(String event, String content) {
        String id = System.currentTimeMillis() + "-" + RandomStringUtils.randomAlphanumeric(8);
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(id).append("\n");
        sb.append("event: ").append(event).append("\n");
        sb.append("data: {");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"content\":").append(jsonEscape(content));
        sb.append("}\n\n");
        return sb.toString();
    }

    /**
     * Properly escape content for JSON inclusion
     */
    public static String jsonEscape(String content) {
        if (content == null) return "null";

        // If content already looks like JSON, don't wrap it in quotes
        if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
            return content;
        } else {
            // Escape quotes and wrap in quotes
            return "\"" + content.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r") + "\"";
        }
    }
}
