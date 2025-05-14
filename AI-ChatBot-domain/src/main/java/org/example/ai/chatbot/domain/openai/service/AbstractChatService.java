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
import org.example.ai.chatbot.types.exception.ChatGPTException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public abstract class AbstractChatService implements IChatService {


    @Resource
    protected IOpenAiRepository iOpenAiRepository;

    @Resource
    private IRebateService rebateService;

    @Resource
    private OllamaChatClient ollamaChatClient;

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
                return Flux.just(formatErrorMessage(Constants.ResponseCode.USER_BANNED.getCode(),
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
                return Flux.just(formatErrorMessage(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(),
                        Constants.ResponseCode.ILLEGAL_PARAMETER.getInfo()));
            }

            // process rebate for each chat session
            try {
                rebateService.rebateGoods(chatProcessAggregate.getOpenid(), RandomStringUtils.randomNumeric(11));
            } catch (Exception e) {
                log.error("point rebate fail, openId:{}", chatProcessAggregate.getOpenid(), e);
                // Continue execution even if rebate fails
            }

            // 3. Process response
            return doMessageResponse(chatProcessAggregate);
        } catch (Exception e) {
            log.error("Unexpected error in generateStream", e);
            return Flux.just(formatErrorMessage(Constants.ResponseCode.UN_ERROR.getCode(),
                    Constants.ResponseCode.UN_ERROR.getInfo()));
        }
    }

    // Helper method to format error messages
    private String formatErrorMessage(String code, String message) {
        return String.format("{\"error\":{\"code\":\"%s\",\"message\":\"%s\"}}", code, message);
    }

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity, String... logics) throws Exception;

    protected abstract Flux<String> doMessageResponse(ChatProcessAggregate chatProcessAggregate) throws Exception;

}
