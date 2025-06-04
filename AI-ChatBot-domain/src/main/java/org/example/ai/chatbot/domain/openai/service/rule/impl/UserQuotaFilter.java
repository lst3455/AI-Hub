package org.example.ai.chatbot.domain.openai.service.rule.impl;

import jakarta.annotation.Resource;
import org.example.ai.chatbot.domain.openai.annotation.LogicStrategy;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.repository.IOpenAiRepository;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.USER_QUOTA)
public class UserQuotaFilter implements ILogicFilter<UserAccountEntity> {

    @Resource
    private IOpenAiRepository openAiRepository;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity) throws Exception {
        if (userAccountEntity.getSurplusQuota() > 0) {
            // Deduct account quota; since it's personal account data with no resource competition, using a database directly is fine.
            // For efficiency, it can also be optimized with Redis for quota deduction.
            int updateCount = openAiRepository.subAccountQuota(userAccountEntity.getOpenid());
            if (updateCount != 0) {
                return RuleLogicEntity.<ChatProcessAggregate>builder()
                        .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
            }

            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .info("Personal account quota of [" + userAccountEntity.getTotalQuota() + "] has been exhausted!")
                    .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("Personal account quota of [" + userAccountEntity.getTotalQuota() + "] has been exhausted!")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }

}
