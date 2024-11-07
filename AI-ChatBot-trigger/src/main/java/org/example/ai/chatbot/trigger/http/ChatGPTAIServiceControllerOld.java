package org.example.ai.chatbot.trigger.http;

import cn.bugstack.chatglm.model.*;
import cn.bugstack.chatglm.session.OpenAiSession;
import okhttp3.Response;
import org.example.ai.chatbot.domain.auth.service.IAuthService;
import org.example.ai.chatbot.trigger.http.dto.ChatGPTRequestDTO;
import org.example.ai.chatbot.trigger.http.dto.MessageEntity;
import org.example.ai.chatbot.types.common.Constants;
import org.example.ai.chatbot.types.exception.ChatGPTException;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v0/chatbot")
public class ChatGPTAIServiceControllerOld {

    @Resource
    private OpenAiSession openAiSession;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private Map<String, Model> modelMap;

    @Resource
    private IAuthService authService;

    /**
     * This method handles streaming requests for ChatGPT responses.
     * It receives a ChatGPTRequestDTO, verifies the provided token, and
     * initiates a conversation with the OpenAI session. It streams responses
     * back to the client until completion.
     *
     * Endpoint: POST /api/v0/chatbot/chat/completions
     *
     * @param request The request payload containing the ChatGPT model and message details.
     * @param token The authorization token header for user authentication.
     * @param response The HttpServletResponse used to configure response headers for streaming.
     * @return ResponseBodyEmitter to send streaming responses to the client.
     */
    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
        log.info("Stream request started with model: {} Request details: {}", request.getModel(), JSON.toJSONString(request.getMessages()));

        try {
            // Basic configuration: streaming, encoding, disabling cache
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);

            // Token verification
            log.info("Starting token verification");
            boolean success = authService.checkToken(token);
            if (!success) {
                log.info("Token verification failed");
                try {
                    emitter.send(Constants.ResponseCode.TOKEN_ERROR.getCode());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                emitter.complete();
                return emitter;
            }

            log.info("Token verification succeeded");

            emitter.onCompletion(() -> {
                log.info("Stream request completed with model: {}", request.getModel());
            });
            emitter.onError(throwable -> {
                log.error("Stream request error with model: {}", request.getModel(), throwable);
            });

            // Initialize chat request
            ChatCompletionRequest chatRequest = new ChatCompletionRequest();
            chatRequest.setModel(modelMap.get(request.getModel())); // Sets the model from the DTO
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
                            .content("Use the same language as the user if not specified.\n" + request.getMessages().stream()
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

            return emitter;
        } catch (Exception e) {
            log.error("Stream response error for model: {}", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }
    }

    /**
     * This method demonstrates a sample streaming response with fixed test data.
     * It sends a series of test messages to the client with a short delay between each message.
     * Primarily used for testing or development purposes.
     *
     * Endpoint: GET /api/v0/chatbot/chat
     *
     * @param response The HttpServletResponse used to configure response headers for streaming.
     * @return ResponseBodyEmitter to send streaming test messages to the client.
     */
    @RequestMapping(value = "/chat", method = RequestMethod.GET)
    public ResponseBodyEmitter completionsStream(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        threadPoolExecutor.execute(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    emitter.send("Test message\r\n" + i);
                    Thread.sleep(100);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            emitter.complete();
        });

        return emitter;
    }
}
