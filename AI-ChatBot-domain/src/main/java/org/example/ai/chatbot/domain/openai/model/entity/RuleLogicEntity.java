package org.example.ai.chatbot.domain.openai.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ai.chatbot.domain.openai.model.valobj.LogicCheckTypeVO;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 规则校验结果实体
 * @create 2023-09-16 17:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleLogicEntity<T> {

    private LogicCheckTypeVO type;
    private String info;
    private T data;

}
