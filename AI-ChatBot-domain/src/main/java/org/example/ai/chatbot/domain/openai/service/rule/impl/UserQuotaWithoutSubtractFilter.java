package org.example.ai.chatbot.domain.openai.service.rule.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.annotation.LogicStrategy;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.USER_QUOTA_NO_SUBTRACT)
public class UserQuotaWithoutSubtractFilter implements ILogicFilter<UserAccountEntity> {

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity) {
        if (userAccountEntity.getSurplusQuota() > 0) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("Personal account quota of [" + userAccountEntity.getTotalQuota() + "] has been exhausted!")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }

}
