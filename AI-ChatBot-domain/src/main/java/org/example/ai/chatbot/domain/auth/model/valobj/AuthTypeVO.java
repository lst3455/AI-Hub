package org.example.ai.chatbot.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Enum representing authentication status types, including success,
 * missing verification code, and invalid verification code.
 * Each type is associated with a unique code and message.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AuthTypeVO {

    A0000("0000", "Verification successful"),
    A0001("0001", "Verification code does not exist"),
    A0002("0002", "Invalid verification code"),
    A0003("0003", "userID and verification code not match");

    private String code;  // Unique code representing the authentication status
    private String info;  // Informational message describing the authentication status

}
