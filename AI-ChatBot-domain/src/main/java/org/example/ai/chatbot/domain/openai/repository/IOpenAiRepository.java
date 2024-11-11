package org.example.ai.chatbot.domain.openai.repository;


import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAi 仓储接口
 * @create 2023-10-03 16:49
 */
public interface IOpenAiRepository {

    int subAccountQuota(String openai);

    UserAccountEntity queryUserAccount(String openid);

    void insertUserAccount(String openid);
}
