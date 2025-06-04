package org.example.ai.chatbot.domain.openai.service;

import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;


public interface IChatService {

    Flux<String> generateStream(ChatProcessAggregate chatProcessAggregate);

}
