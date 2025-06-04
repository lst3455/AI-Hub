package org.example.ai.chatbot.domain.openai.service.rule.impl;

import com.alibaba.fastjson2.JSON;
import com.google.common.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.annotation.LogicStrategy;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCESS_LIMIT)
public class AccessLimitFilter implements ILogicFilter<UserAccountEntity> {

    @Value("${app.config.limit-count:10}")
    private Integer limitCount;
    @Value("${app.config.white-list}")
    private String whiteListStr;
    @Resource
    private Cache<String, Integer> visitCache;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity) throws Exception {
        log.info("rule engine - accessLimit - start, chatProcess:{}", JSON.toJSONString(chatProcess));

        // 1. Whitelist users are granted direct access
        if (chatProcess.isWhiteList(whiteListStr)) {
            log.info("rule engine - accessLimit - whitelist user, pass, openId:{}", chatProcess.getOpenid());
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }
        String openid = chatProcess.getOpenid();

        // 2. Check access count
        int visitCount = visitCache.get(openid, () -> 0);
        if (visitCount < limitCount) {
            visitCache.put(openid, visitCount + 1);
            log.info("rule engine - accessLimit - pass, visitCount:{}", visitCache.getIfPresent(openid));
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }
        log.info("rule engine - accessLimit - takeover, not enough free attempts remaining");
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("Your " + limitCount + " free attempts for today have been used up!")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }
}
