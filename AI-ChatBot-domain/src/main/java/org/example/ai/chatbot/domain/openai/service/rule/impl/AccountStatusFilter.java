package org.example.ai.chatbot.domain.openai.service.rule.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.annotation.LogicStrategy;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.model.valobj.UserAccountStatusVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountEntity> {

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity) throws Exception {
        // Account is available, allow access
        if (UserAccountStatusVO.AVAILABLE.equals(userAccountEntity.getUserAccountStatusVO())) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("Your account has been frozen and is temporarily unavailable. If you have any questions, please contact customer service to unfreeze your account.")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }

}
