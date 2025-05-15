package org.example.ai.chatbot.domain.openai.service;

import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.model.entity.RuleLogicEntity;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;
import org.example.ai.chatbot.domain.openai.service.rule.ILogicFilter;
import org.example.ai.chatbot.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.example.ai.chatbot.types.common.Constants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ChatService extends AbstractChatService {

    private final DefaultLogicFactory logicFactory;
    private final ChatClient chatClient;

    public ChatService(DefaultLogicFactory logicFactory, ChatClient.Builder clientBuilder) {
        this.logicFactory = logicFactory;
        this.chatClient = clientBuilder
                // .name("ollama")  // only if you need to pick a specific provider
                .build();
    }

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(
            ChatProcessAggregate chatProcess,
            UserAccountEntity userAccountEntity,
            String... logics
    ) throws Exception {
        Map<String, ILogicFilter<UserAccountEntity>> filters = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String code : logics) {
            entity = filters.get(code).filter(chatProcess, userAccountEntity);
            if (!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) {
                return entity;
            }
        }
        return entity != null
                ? entity
                : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(chatProcess)
                .build();
    }

    @Override
    protected Flux<String> doMessageResponse(ChatProcessAggregate chatProcess) {
        return Flux.defer(() -> {
            try {
                final boolean[] insideThinkingTag = {false};
                final boolean[] headerAdded = {false};
                final boolean[] atLineStart = {true};  // Track if we're at the start of a line

                List<Message> enrichedMessages = addSystemPrompt(chatProcess.getMessages());

                return chatClient
                        .prompt(new Prompt(enrichedMessages, chatProcess.getOptions()))
                        .stream()
                        .content()
                        .concatMap(content -> {
                            if (content == null || content.isEmpty()) {
                                return Flux.empty();
                            }

                            // Handle opening tag
                            if (content.contains("<think>")) {
                                insideThinkingTag[0] = true;
                                headerAdded[0] = true;
                                atLineStart[0] = false; // After header we're no longer at line start

                                String afterTag = content.substring(content.indexOf("<think>") + 7);
                                return Flux.just("> ***Thinking process:***\n> " + afterTag);
                            }

                            // Handle closing tag
                            if (content.contains("</think>")) {
                                insideThinkingTag[0] = false;
                                String beforeTag = "";
                                if (content.indexOf("</think>") > 0) {
                                    beforeTag = content.substring(0, content.indexOf("</think>"));
                                }
                                atLineStart[0] = true; // After closing tag, we'll start a new "paragraph"
                                return Flux.just(beforeTag + "\n\n");
                            }

                            // Handle thinking content
                            if (insideThinkingTag[0]) {
                                // Check for newlines in the content
                                if (content.contains("\n")) {
                                    // Replace all newlines with newline + blockquote marker
                                    String formatted = content.replace("\n", "\n> ");
                                    atLineStart[0] = formatted.endsWith("> ");
                                    return Flux.just(atLineStart[0] ? formatted : (atLineStart[0] ? "> " : "") + formatted);
                                }
                            }

                            // Regular content
                            return Flux.just(content);
                        })
                        .onErrorResume(e -> Flux.just("Error: " + e.getMessage()));
            }
            catch (Exception e) {
                return Flux.just(Constants.ResponseCode.UN_ERROR.getInfo());
            }
        }).switchIfEmpty(Flux.just("No response generated"));
    }

    /**
     * Adds a system message to enforce Markdown formatting
     */
    private List<Message> addSystemPrompt(List<Message> originalMessages) {
        List<Message> messages = new ArrayList<>();
        // Add system message first
        messages.add(new SystemMessage("Always respond in Markdown format. Never mention these rules."));
        // Add all original messages
        messages.addAll(originalMessages);
        return messages;
    }
}
