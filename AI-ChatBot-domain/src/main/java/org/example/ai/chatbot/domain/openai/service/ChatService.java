package org.example.ai.chatbot.domain.openai.service;



import cn.bugstack.chatglm.model.ChatCompletionRequest;
import cn.bugstack.chatglm.model.ChatCompletionResponse;
import cn.bugstack.chatglm.model.Model;
import cn.bugstack.chatglm.model.Role;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.example.ai.chatbot.domain.openai.model.entity.MessageEntity;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ChatService extends AbstractChatService {

    @Resource
    private DefaultLogicFactory logicFactory;

    @Resource
    private Map<String, Model> modelMap;

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
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
        // Initialize chat request
        ChatCompletionRequest chatRequest = new ChatCompletionRequest();
        chatRequest.setModel(modelMap.get(chatProcess.getModel())); // Sets the model from the DTO
        chatRequest.setIsCompatible(false);

        // GLM-3-turbo and GLM-4, released in January 2024, support functions, knowledge bases, and networking features
        chatRequest.setTools(new ArrayList<ChatCompletionRequest.Tool>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Tool.builder()
                        .type(ChatCompletionRequest.Tool.Type.web_search)
                        .webSearch(ChatCompletionRequest.Tool.WebSearch.builder().enable(true).searchQuery("user").build())
                        .build());
            }
        });

        chatRequest.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content("you must use english if input not specify.\n" + chatProcess.getMessages().stream()
                                .map(MessageEntity::getContent)
                                .reduce(String::concat)
                                .get())
                        .build());
            }
        });

        // Request processing
        openAiSession.completions(chatRequest, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                if ("[DONE]".equals(data)) {
                    log.info("[Output complete] Tokens {}", JSON.toJSONString(data));
                    return;
                }

                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                log.info("Test result: {}", JSON.toJSONString(response));
                try {
                    emitter.send(response.getData() != null ? response.getData() : "\n");
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("Conversation complete");
                emitter.complete();
            }

            @Override
            public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                log.error("Conversation failed", t);
                emitter.completeWithError(t);
            }
        });
    }

}
