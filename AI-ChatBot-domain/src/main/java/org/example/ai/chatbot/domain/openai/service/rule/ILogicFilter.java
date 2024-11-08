package org.example.ai.chatbot.domain.openai.service.rule;


import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 规则过滤接口
 * @create 2023-09-16 16:59
 */
public interface ILogicFilter {

    RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess) throws Exception;

}
