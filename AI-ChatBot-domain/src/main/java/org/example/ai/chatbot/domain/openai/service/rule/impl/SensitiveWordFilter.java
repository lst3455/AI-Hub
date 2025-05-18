package org.example.ai.chatbot.domain.openai.service.rule.impl;

import com.alibaba.fastjson2.JSON;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.annotation.LogicStrategy;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.SENSITIVE_WORD)
public class SensitiveWordFilter implements ILogicFilter<UserAccountEntity> {

    @Resource
    private SensitiveWordBs words;

    @Value("${app.config.white-list}")
    private String whiteListStr;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(
            ChatProcessAggregate chatProcess,
            UserAccountEntity userAccountEntity) throws Exception {

        log.info("rule engine - sensitive - start, chatProcess:{}", JSON.toJSONString(chatProcess));

        // 1) Whitelist bypass
        if (chatProcess.isWhiteList(whiteListStr)) {
            log.info("rule engine - sensitive - whitelist user, pass, openId:{}", chatProcess.getOpenid());
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .data(chatProcess)
                    .build();
        }

        // 2) Rebuild aggregate with sanitized messages
        ChatProcessAggregate sanitized = new ChatProcessAggregate();
        sanitized.setOpenid(chatProcess.getOpenid());
        sanitized.setModel(chatProcess.getModel());

        List<Message> filtered = chatProcess.getMessages().stream()
                .map(msg -> {
                    // 1) Replace sensitive words and get updated text
                    String replaced = words.replace(msg.getText());             // use getText() :contentReference[oaicite:8]{index=8}

                    // 2) Branch based on message role
                    return switch (msg.getMessageType()) {
                        case USER ->
                                new UserMessage(replaced);                         // UserMessage(String) :contentReference[oaicite:9]{index=9}
                        case ASSISTANT ->
                                new AssistantMessage(replaced);                    // AssistantMessage(String) :contentReference[oaicite:10]{index=10}
                        case SYSTEM ->
                                new SystemMessage(replaced);                       // SystemMessage(String) :contentReference[oaicite:11]{index=11}
                        default -> throw new IllegalArgumentException(
                                "Unsupported message type: " + msg.getMessageType());
                    };
                })
                .collect(Collectors.toList());

        sanitized.setMessages(filtered);
        log.info("rule engine - sensitive - pass, openId:{}", chatProcess.getOpenid());

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(sanitized)
                .build();
    }
}
