package org.example.ai.chatbot.domain.openai.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户状态
 * @create 2023-10-03 17:26
 */
@Getter
@AllArgsConstructor
public enum UserAccountStatusVO {

    AVAILABLE(0, "available"),
    FREEZE(1,"unavailable"),
    ;

    private final Integer code;
    private final String info;

    public static UserAccountStatusVO get(Integer code){
        switch (code){
            case 0:
                return UserAccountStatusVO.AVAILABLE;
            case 1:
                return UserAccountStatusVO.FREEZE;
            default:
                return UserAccountStatusVO.AVAILABLE;
        }
    }

}
