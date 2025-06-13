package org.example.ai.chatbot.domain.openai.service;

import org.example.ai.chatbot.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.chatbot.types.exception.AiServiceException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutionException;


public interface IChatService {

    Flux<String> generateStream(ChatProcessAggregate chatProcessAggregate) throws AiServiceException, ExecutionException;

    Flux<String> generateTitle(ChatProcessAggregate chatProcessAggregate) throws AiServiceException, ExecutionException;

}
