package org.example.ai.chatbot.trigger.http;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.domain.auth.service.IAuthService;
import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.domain.openai.service.IChatService;
import org.example.ai.chatbot.trigger.http.dto.ChatGPTRequestDTO;
import org.example.ai.chatbot.types.common.Constants;
import org.example.ai.chatbot.types.exception.ChatGPTException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController()
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}/chatbot/ollama")
@Slf4j
public class OllamaSeriveController {


    @Resource
    private IAuthService authService;

    @Resource
    private IChatService chatService;


    /**
     * http://localhost:8090/api/v0/chatbot/ollama/generate_stream
     */
    @RequestMapping(value = "generate_stream", method = RequestMethod.POST)
    public Flux<String> generateStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
        log.info("trigger generate, request:{}", JSON.toJSONString(request));

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

            // 4. Convert DTO messages to Spring AI messages
            List<Message> aiMessages = request.getMessages().stream()
                    .map(msg -> {
                        switch (msg.getRole()) {
                            case "user": return new UserMessage(msg.getContent());
                            case "system": return new SystemMessage(msg.getContent());
                            case "assistant": return new AssistantMessage(msg.getContent());
                            default: return new UserMessage(msg.getContent());
                        }
                    })
                    .collect(Collectors.toList());

            // 4. Build parameters
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(aiMessages)
                    .build();

            // 6. Stream the response - extract just the text content from each ChatResponse
            return chatService.generateStream(chatProcessAggregate);
        } catch (Exception e) {
            log.error("Streaming response, request: {} encountered an exception", request, e);
            throw new ChatGPTException(e.getMessage());
        }
    }
}