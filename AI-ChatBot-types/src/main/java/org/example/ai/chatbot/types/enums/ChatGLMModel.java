package org.example.ai.chatbot.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 模型对象
 * @create 2023-07-22 21:00
 */
@Getter
@AllArgsConstructor
public enum ChatGLMModel {

    GLM_4("GLM_4"),
    GLM_3_5_TURBO("GLM_3_5_TURBO")

    ;
    private final String code;

}
