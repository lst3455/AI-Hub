package org.example.ai.chatbot.domain.openai.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 逻辑校验类型，值对象
 * @create 2023-09-16 17:04
 */
@Getter
@AllArgsConstructor
public enum LogicCheckTypeVO {

    SUCCESS("0000", "校验通过"),
    REFUSE("0001","校验拒绝"),
            ;

    private final String code;
    private final String info;

}
