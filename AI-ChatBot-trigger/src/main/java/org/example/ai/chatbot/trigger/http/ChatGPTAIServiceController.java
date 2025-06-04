//package org.example.ai.chatbot.trigger.http;
//
//import jakarta.servlet.http.HttpServletResponse;
//import org.example.ai.chatbot.domain.auth.service.IAuthService;
//import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
//import org.example.ai.chatbot.domain.openai.model.entity.MessageEntity;
//import org.example.ai.chatbot.domain.openai.service.IChatService;
//import org.example.ai.chatbot.trigger.http.dto.ChatGPTRequestDTO;
//import org.example.ai.chatbot.types.common.Constants;
//import org.example.ai.chatbot.types.exception.ChatGPTException;
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
//
//import jakarta.annotation.Resource;
//
//import java.io.IOException;
//import java.util.stream.Collectors;
//
//
//@Slf4j
//@RestController()
//@CrossOrigin("${app.config.cross-origin}")
//@RequestMapping("/api/${app.config.api-version}/chatbot")
//public class ChatGPTAIServiceController {
//
//    @Resource
//    private IChatService chatService;
//
//    @Resource
//    private IAuthService authService;
//
//    /**
//     * Streaming Question and Answer, ChatGPT Request Interface
//     * <p>
//     * Example:
//     * curl -X POST \
//     * http://localhost:8090/api/v1/chat/completions \
//     * -H 'Content-Type: application/json;charset=utf-8' \
//     * -H 'Authorization: b8b6' \
//     * -d '{
//     * "messages": [
//     * {
//     * "content": "Write a Java bubble sort",
//     * "role": "user"
//     * }
//     * ],
//     * "model": "gpt-3.5-turbo"
//     * }'
//     */
//    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
//    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
//        log.info("Starting streaming Q&A request, using model: {} Request data: {}", request.getModel(), JSON.toJSONString(request.getMessages()));
//        try {
//            // 1. Basic configuration: stream output, encoding, disable caching
//            response.setContentType("text/event-stream");
//            response.setCharacterEncoding("UTF-8");
//            response.setHeader("Cache-Control", "no-cache");
//
//            // 2. Build asynchronous response object [Intercept expired tokens]
//            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
//
//            emitter.onCompletion(() -> {
//                log.info("Stream request completed with model: {}", request.getModel());
//            });
//            emitter.onError(throwable -> {
//                log.error("Stream request error with model: {}", request.getModel(), throwable);
//            });
//
//            // Token verification
//            log.info("Starting token verification");
//            boolean success = authService.checkToken(token);
//            if (!success) {
//                log.info("Token verification failed");
//                try {
//                    emitter.send(Constants.ResponseCode.TOKEN_ERROR.getCode());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                emitter.complete();
//                return emitter;
//            }
//
//            log.info("Token verification succeeded");
//
//            // 3. Get OpenID
//            String openid = authService.openid(token);
//            log.info("Processing streaming Q&A request, openid: {} Request model: {}", openid, request.getModel());
//
//            // 4. Build parameters
//            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
//                    .openid(openid)
//                    .model(request.getModel())
//                    .messages(request.getMessages().stream()
//                            .map(entity -> MessageEntity.builder()
//                                    .role(entity.getRole())
//                                    .content(entity.getContent())
//                                    .name(entity.getName())
//                                    .build())
//                            .collect(Collectors.toList()))
//                    .build();
//
//            // 5. Send request & return results
//            return chatService.completions(emitter, chatProcessAggregate);
//        } catch (Exception e) {
//            log.error("Streaming response, request model: {} encountered an exception", request.getModel(), e);
//            throw new ChatGPTException(e.getMessage());
//        }
//    }
//}
