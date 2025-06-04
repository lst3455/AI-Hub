package org.example.ai.chatbot.trigger.http.dto;

import org.example.ai.chatbot.types.enums.ChatGLMModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTRequestDTO {

    /** 默认模型 */
    private String model;

    /** 问题描述 */
    private List<MessageEntity> messages;

}
