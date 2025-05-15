package org.example.ai.chatbot.domain.openai.service;



import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.example.ai.chatbot.types.common.Constants;
import org.example.ai.chatbot.types.exception.ChatGPTException;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;


@Service
@Slf4j
public class ChatService extends AbstractChatService {

    @Resource
    private DefaultLogicFactory logicFactory;

    @Resource
    private OllamaChatClient ollamaChatClient;

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountEntity userAccountEntity, String... logics) throws Exception {
        Map<String, ILogicFilter<UserAccountEntity>> logicFilterMap = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String code : logics) {
            entity = logicFilterMap.get(code).filter(chatProcess, userAccountEntity);
            if (!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) return entity;
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
    }

    @Override
    protected Flux<String> doMessageResponse(ChatProcessAggregate chatProcessAggregate) {
        log.debug("Processing message response for model: {}",
                chatProcessAggregate.getOptions().getModel());

        return Flux.defer(() -> {
            try {
                return ollamaChatClient.stream(new Prompt(chatProcessAggregate.getMessages(),
                                chatProcessAggregate.getOptions()))
                        .mapNotNull(chatResponse -> {
                            if (chatResponse.getResult() != null
                                    && chatResponse.getResult().getOutput() != null
                                    && chatResponse.getResult().getOutput().getContent() != null) {
                                String content = chatResponse.getResult().getOutput().getContent();

                                // Skip thinking content and empty responses
                                if (content.isEmpty() || content.startsWith("<think>") ||
                                        content.startsWith("</think>") || content.equals("\n")) {
                                    return null;
                                }

                                return content;
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .onErrorResume(e -> {
                            log.error("Error while streaming response", e);
                            return Flux.just("Error processing request: " + e.getMessage());
                        });
            } catch (Exception e) {
                log.error("Failed to initialize streaming response", e);
                return Flux.just(Constants.ResponseCode.UN_ERROR.getInfo());
            }
        }).switchIfEmpty(Flux.just("No response generated"));
    }
}
