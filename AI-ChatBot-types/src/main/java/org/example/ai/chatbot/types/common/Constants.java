package org.example.ai.chatbot.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class defines constants used throughout the application, including response codes
 * for various outcomes like success, errors, and permission issues.
 */
public class Constants {

    /**
     * Enum to represent different response codes and their associated messages.
     * Each code includes a unique identifier and an informational message.
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode {
        SUCCESS("0000", "Success"),
        UN_ERROR("0001", "Unknown failure"),
        ILLEGAL_PARAMETER("0002", "Invalid parameter"),
        TOKEN_ERROR("0003", "Permission denied");

        private String code;  // Unique code representing the response type
        private String info;  // Informational message associated with the response code
    }
}
