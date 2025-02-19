package org.example.ai.chatbot.domain.openai.service;

import cn.bugstack.chatglm.session.OpenAiSession;
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
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractChatService implements IChatService {

    @Resource
    protected OpenAiSession openAiSession;

    @Resource
    protected IOpenAiRepository iOpenAiRepository;

    @Resource
    private IRebateService rebateService;

    @Override
    public ResponseBodyEmitter completions(ResponseBodyEmitter emitter, ChatProcessAggregate chatProcess) {
        openAiSession.configuration().setApiSecretKey("8116aa8a1aad4203993b2b78054d61e6.YO0YtPL1ludEPlDJ"); //todo for some unknown reason, can not initialize secretKey correctly, so do the hardcode here
        log.info("current key: {}, secert: {}",openAiSession.configuration().getApiKey(), openAiSession.configuration().getApiSecret());
        try {
            log.info("current token: {}",openAiSession.configuration().getApiKey());
            // 1. Get user account
            UserAccountEntity userAccountEntity = iOpenAiRepository.queryUserAccount(chatProcess.getOpenid());
            // If account does not exist, create a new user account
            if (userAccountEntity == null) {
                iOpenAiRepository.insertUserAccount(chatProcess.getOpenid());
                userAccountEntity = iOpenAiRepository.queryUserAccount(chatProcess.getOpenid());
            }

            // 2. Apply rule filters
            // First, check the account status
            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess,
                    userAccountEntity,
                    userAccountEntity != null ? DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode()
            );

            // If the account is unavailable, return a message
            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                emitter.send(ruleLogicEntity.getInfo());
                emitter.complete();
                return emitter;
            } else {
                // If available, check other filter
                ruleLogicEntity = this.doCheckLogic(chatProcess,
                        userAccountEntity,
                        userAccountEntity != null ? DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                        userAccountEntity != null ? DefaultLogicFactory.LogicModel.USER_QUOTA.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                        DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode()
                );
            }

            // If any rule fails, return a message
            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                emitter.send(ruleLogicEntity.getInfo());
                emitter.complete();
                return emitter;
            }

            // process rebate for each chat session
            try{
                rebateService.rebateGoods(chatProcess.getOpenid(), RandomStringUtils.randomNumeric(11)); // todo check if can use this to replace orderId
            }catch (Exception e){
                log.error("point rebate fail, openId:{}",chatProcess.getOpenid(),e);
            }

            // 3. Process response
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4. Return the response
        return emitter;
    }

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity, String... logics) throws Exception;

    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws Exception;

}
