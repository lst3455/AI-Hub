package org.example.ai.chatbot.domain.openai.service.rule.impl;

import com.alibaba.fastjson2.JSON;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.annotation.LogicStrategy;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.MessageEntity;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
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
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity) throws Exception {
        log.info("rule engine - sensitive - start, chatProcess:{}", JSON.toJSONString(chatProcess));

        // Whitelisted users are exempt from sensitive word filtering
        if (chatProcess.isWhiteList(whiteListStr)) {
            log.info("rule engine - sensitive - whitelist user, pass, openId:{}", chatProcess.getOpenid());
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        ChatProcessAggregate newChatProcessAggregate = new ChatProcessAggregate();
        newChatProcessAggregate.setOpenid(chatProcess.getOpenid());
        newChatProcessAggregate.setModel(chatProcess.getModel());

        List<MessageEntity> newMessages = chatProcess.getMessages().stream()
                .map(message -> {
                    String content = message.getContent();
                    String replace = words.replace(content);
                    return MessageEntity.builder()
                            .role(message.getRole())
                            .name(message.getName())
                            .content(replace)
                            .build();
                })
                .collect(Collectors.toList());

        newChatProcessAggregate.setMessages(newMessages);
        log.info("rule engine - sensitive - pass, openId:{}", chatProcess.getOpenid());
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(newChatProcessAggregate)
                .build();
    }

}
