package org.example.ai.chatbot.trigger.http.dto;

import org.example.ai.chatbot.types.enums.ChatGLMModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 21:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTRequestDTO {

    /** 默认模型 */
    private String model = ChatGLMModel.GLM_3_5_TURBO.getCode();

    /** 问题描述 */
    private List<MessageEntity> messages;

}
