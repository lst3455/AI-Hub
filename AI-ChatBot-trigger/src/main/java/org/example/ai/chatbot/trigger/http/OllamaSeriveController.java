package org.example.ai.chatbot.trigger.http;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.chatbot.trigger.http.dto.ChatGPTRequestDTO;
import org.example.ai.chatbot.types.exception.ChatGPTException;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

@RestController()
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}/chatbot/ollama")
@Slf4j
public class OllamaSeriveController{

    @Resource
    private OllamaChatClient ollamaChatClient;

    /**
     * http://localhost:8090/api/v0/chatbot/ollama/generate_stream
     */
    @RequestMapping(value = "generate_stream", method = RequestMethod.POST)
    public Flux<ChatResponse> generateStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
        log.info("trigger generate, request:{}", JSON.toJSONString(request));

        try{
            // 1. Basic configuration: stream output, encoding, disable caching
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            return ollamaChatClient.stream(new Prompt(request.getMessages().toString(), OllamaOptions.create().withModel("deepseek-r1:1.5b")));
        }catch (Exception e){
            log.error("Streaming response, request: {} encountered an exception", request, e);
            throw new ChatGPTException(e.getMessage());
        }
    }
}
