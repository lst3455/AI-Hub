package org.example.ai.chatbot.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ChatGLMModel {

    GLM_4("GLM_4"),
    GLM_3_5_TURBO("GLM_3_5_TURBO")

    ;
    private final String code;

}
