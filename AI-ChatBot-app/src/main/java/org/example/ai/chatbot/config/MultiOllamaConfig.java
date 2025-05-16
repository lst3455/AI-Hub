package org.example.ai.chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Current support Qwen3:1.7b, Qwen3:8b, Qwen3:14b
 */

@Configuration
public class MultiOllamaConfig {

    // 1) Host A → qwen3:1.7b
    @Bean("qwen3_1_7bApi")
    @Primary
    public OllamaApi ollamaApiQwen3_1_7b() {
        return OllamaApi.builder()
                .baseUrl("http://117.72.127.104:11434")
                .build();
    }

    @Bean("qwen3_1_7bChatModel")
    public OllamaChatModel qwen3_1_7bChatModel(
            @Qualifier("qwen3_1_7bApi") OllamaApi api) {
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:1.7b")
                                .build()
                )
                .build();
    }

    // 2) Host B → qwen3:14b
    @Bean("qwen3_8bApi")
    public OllamaApi ollamaApiQwen3_8b() {
        return OllamaApi.builder()
                .baseUrl("http://8.219.99.73:11434")
                .build();
    }

    @Bean("qwen3_8bChatModel")
    public OllamaChatModel qwen3_8bChatModel(
            @Qualifier("qwen3_8bApi") OllamaApi api) {
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:8b")
                                .build()
                )
                .build();
    }

    // 2) Host B → qwen3:14b
    @Bean("qwen3_14bApi")
    public OllamaApi ollamaApiQwen3_14b() {
        return OllamaApi.builder()
                .baseUrl("http://8.219.99.73:11434")
                .build();
    }

    @Bean("qwen3_14bChatModel")
    public OllamaChatModel qwen3_14bChatModel(
            @Qualifier("qwen3_14bApi") OllamaApi api) {
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:14b")
                                .build()
                )
                .build();
    }

    // 3) ChatClient beans for each model
    @Bean("chatClient_qwen3_1_7b")
    public ChatClient chatClientQwen3_7b(
            @Qualifier("qwen3_1_7bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_8b")
    public ChatClient chatClientQwen3_8b(
            @Qualifier("qwen3_8bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_14b")
    public ChatClient chatClientQwen3_14b(
            @Qualifier("qwen3_14bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }
}