package org.example.ai.chatbot.domain.openai.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.repository.IOpenAiRepository;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.example.ai.chatbot.domain.rebate.service.IRebateService;
import org.example.ai.chatbot.types.common.Constants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public abstract class AbstractChatService implements IChatService {


    @Resource
    protected IOpenAiRepository iOpenAiRepository;

    @Resource
    private IRebateService rebateService;


    @Override
    public Flux<String> generateStream(ChatProcessAggregate chatProcessAggregate) {
        try {
            // 1. Get user account
            UserAccountEntity userAccountEntity = iOpenAiRepository.queryUserAccount(chatProcessAggregate.getOpenid());
            // If account does not exist, create a new user account
            if (userAccountEntity == null) {
                iOpenAiRepository.insertUserAccount(chatProcessAggregate.getOpenid());
                userAccountEntity = iOpenAiRepository.queryUserAccount(chatProcessAggregate.getOpenid());
            }

            // 2. Apply rule filters
            // First, check the account status
            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcessAggregate,
                    userAccountEntity,
                    userAccountEntity != null ? DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode()
            );

            // If the account is unavailable, return error message as Flux
            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                return Flux.just(formatSseMessage("error", Constants.ResponseCode.USER_BANNED.getCode(),
                        Constants.ResponseCode.USER_BANNED.getInfo()));
            }

            // If available, check other filter
            ruleLogicEntity = this.doCheckLogic(chatProcessAggregate,
                    userAccountEntity,
                    userAccountEntity != null ? DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                    userAccountEntity != null ? DefaultLogicFactory.LogicModel.USER_QUOTA.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                    DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode()
            );

            // If any rule fails, return error message as Flux
            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                return Flux.just(formatSseMessage("error", Constants.ResponseCode.QUOTA_OR_MODEL_TYPE_UNSUPPORTED.getCode(),
                        Constants.ResponseCode.QUOTA_OR_MODEL_TYPE_UNSUPPORTED.getInfo()));
            }

            // process rebate for each chat session
            try {
                rebateService.rebateGoods(chatProcessAggregate.getOpenid(), RandomStringUtils.randomNumeric(11));
            } catch (Exception e) {
                log.error("point rebate fail, openId:{}", chatProcessAggregate.getOpenid(), e);
                // Continue execution even if rebate fails
            }

            // 3. Process response and transform to SSE format
            return doMessageResponse(chatProcessAggregate)
                    .map(content -> formatSseMessage("message", Constants.ResponseCode.SUCCESS.getCode(), content));
        } catch (Exception e) {
            log.error("Unexpected error in generateStream", e);
            return Flux.just(formatSseMessage("error", Constants.ResponseCode.UN_ERROR.getCode(),
                    Constants.ResponseCode.UN_ERROR.getInfo()));
        }
    }

    /**
     * Format response as Server-Sent Event with proper structure
     * @param event The event type (message, error, etc.)
     * @param code Status code
     * @param content Message content
     * @return Formatted SSE message
     */
    private String formatSseMessage(String event, String code, String content) {
        String id = System.currentTimeMillis() + "-" + RandomStringUtils.randomAlphanumeric(8);
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(id).append("\n");
        sb.append("event: ").append(event).append("\n");
        sb.append("data: {");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"code\":\"").append(code).append("\",");
        sb.append("\"content\":").append(jsonEscape(content));
        sb.append("}\n\n");
        return sb.toString();
    }

    /**
     * Properly escape content for JSON inclusion
     */
    private String jsonEscape(String content) {
        if (content == null) return "null";

        // If content already looks like JSON, don't wrap it in quotes
        if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
            return content;
        } else {
            // Escape quotes and wrap in quotes
            return "\"" + content.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r") + "\"";
        }
    }

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity, String... logics) throws Exception;

    protected abstract Flux<String> doMessageResponse(ChatProcessAggregate chatProcessAggregate) throws Exception;

}
