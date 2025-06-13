package org.example.ai.chatbot.trigger.http;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.auth.service.IAuthService;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.service.IChatService;
import org.example.ai.chatbot.domain.utils.Utils;
import org.example.ai.chatbot.trigger.http.dto.ChatGPTRequestDTO;
import org.example.ai.chatbot.trigger.http.dto.MessageEntity;
import org.example.ai.chatbot.types.common.Constants;
import org.example.ai.chatbot.types.exception.AiServiceException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for AI Chatbot services.
 * Provides endpoints for generating chat responses and titles using streaming.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}/chatbot/ai")
@Slf4j
public class AiSeriveController {

    @Resource
    private IAuthService authService;

    @Resource
    private IChatService chatService;

    /**
     * Generates a streaming chat response.
     * Endpoint: POST /api/v0/chatbot/ai/generate_stream
     *
     * @param request ChatGPT request DTO containing messages and model info
     * @param token   Authorization token
     * @param response HTTP servlet response for SSE
     * @return Flux<String> streaming chat response
     */
    @RequestMapping(value = "generate_stream", method = RequestMethod.POST)
    public Flux<String> generateStream(@RequestBody ChatGPTRequestDTO request,
                                       @RequestHeader("Authorization") String token,
                                       HttpServletResponse response) {
        log.info("Trigger generate general response, request: {}", JSON.toJSONString(request));

        try {
            // 1. Basic configuration: stream output, encoding, disable caching
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. Token verification
            boolean success = authService.checkToken(token);
            if (!success) {
                log.info("Token verification failed");
                return Flux.just(Constants.ResponseCode.TOKEN_ERROR.getCode());
            }

            log.info("Token verification succeeded");

            // 3. Get OpenID
            String openid = authService.openid(token);
            log.info("Processing streaming Q&A request, openid: {} Request model: {}", openid, request.getModel());

            List<MessageEntity> messageEntities = request.getMessages();
            // Avoid empty messageEntities
            if (messageEntities.isEmpty()) {
                messageEntities.add(MessageEntity.builder()
                        .role("user")
                        .content("Hi")
                        .build());
            }

            // 4. Convert DTO messages to Spring AI messages
            List<Message> aiMessages = request.getMessages().stream()
                    .map(msg -> {
                        switch (msg.getRole()) {
                            case "user":
                                return new UserMessage(msg.getContent());
                            case "system":
                                return new SystemMessage(msg.getContent());
                            case "assistant":
                                return new AssistantMessage(msg.getContent());
                            default:
                                return new UserMessage(msg.getContent());
                        }
                    })
                    .collect(Collectors.toList());

            // 5. Build parameters
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(aiMessages)
                    .build();

            // 6. Stream the response - extract just the text content from each ChatResponse
            return chatService.generateStream(chatProcessAggregate);
        } catch (AiServiceException e) {
            log.error("AI service error, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", e.getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("General generate failed, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", Constants.ResponseCode.UN_ERROR.getCode(),
                    Constants.ResponseCode.UN_ERROR.getInfo()));
        }
    }

    /**
     * Generates a streaming title for the chat.
     * Endpoint: POST /api/v0/chatbot/ai/generate_title
     *
     * @param request ChatGPT request DTO containing messages and model info
     * @param token   Authorization token
     * @param response HTTP servlet response for SSE
     * @return Flux<String> streaming title response
     */
    @RequestMapping(value = "generate_title", method = RequestMethod.POST)
    public Flux<String> generateTitle(@RequestBody ChatGPTRequestDTO request,
                                      @RequestHeader("Authorization") String token,
                                      HttpServletResponse response) {
        log.info("Trigger generate title, request: {}", JSON.toJSONString(request));

        try {
            // 1. Basic configuration: stream output, encoding, disable caching
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. Token verification
            boolean success = authService.checkToken(token);
            if (!success) {
                log.info("Token verification failed");
                return Flux.just(Constants.ResponseCode.TOKEN_ERROR.getCode());
            }

            log.info("Token verification succeeded");

            // 3. Get OpenID
            String openid = authService.openid(token);
            log.info("Processing streaming title request, openid: {} Request model: {}", openid, request.getModel());

            List<MessageEntity> messageEntities = request.getMessages() != null ? request.getMessages() : new ArrayList<>();
            // Avoid empty messageEntities
            if (messageEntities.isEmpty()) {
                messageEntities.add(MessageEntity.builder()
                        .role("user")
                        .content("Hi")
                        .build());
            }

            // 4. Convert DTO messages to Spring AI messages
            List<Message> aiMessages = messageEntities.stream()
                    .map(msg -> {
                        switch (msg.getRole()) {
                            case "user":
                                return new UserMessage(msg.getContent());
                            case "system":
                                return new SystemMessage(msg.getContent());
                            case "assistant":
                                return new AssistantMessage(msg.getContent());
                            default:
                                return new UserMessage(msg.getContent());
                        }
                    })
                    .collect(Collectors.toList());

            // 5. Build parameters
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(aiMessages)
                    .build();

            // 6. Stream the response - extract just the text content from each ChatResponse
            return chatService.generateTitle(chatProcessAggregate);
        } catch (AiServiceException e) {
            log.error("AI service error, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", e.getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("Generate title failed, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", Constants.ResponseCode.UN_ERROR.getCode(),
                    Constants.ResponseCode.UN_ERROR.getInfo()));
        }
    }
}