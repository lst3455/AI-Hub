package org.example.ai.chatbot.domain.openai.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogicCheckTypeVO {

    SUCCESS("0000", "pass"),
    REFUSE("0001","refuse"),
            ;

    private final String code;
    private final String info;

}
